package com.example.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Fully-offline audio playback for prayers.
 *
 * Uses the device's TextToSpeech engine with an on-device Arabic voice.
 * If the default engine has no embedded Arabic voice, other installed
 * engines are searched automatically and the first one with an offline
 * Arabic voice is used. Network synthesis is never forced: when an
 * offline voice is active, speech is locked to on-device; otherwise the
 * engine falls back to whatever it can do (possibly network).
 */
class PrayerReciter(context: Context) {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    @Volatile private var hunting = false

    var isReady by mutableStateOf(false)
        private set
    var isSpeaking by mutableStateOf(false)
        private set
    /** Index (into the supplied verse list) of the verse currently being read, or -1. */
    var currentIndex by mutableStateOf(-1)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    /** True when no Arabic voice at all is available — needs a one-time download. */
    var needsVoiceData by mutableStateOf(false)
        private set
    /** True when a fully on-device (no-network) Arabic voice is active. */
    var offlineReady by mutableStateOf(false)
        private set

    private var verses: List<String> = emptyList()
    private var verseIndices: List<Int> = emptyList()
    private var pos = 0

    init {
        initEngine(null)
    }

    private fun initEngine(enginePkg: String?) {
        val engine = TextToSpeech(appContext, { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.setSpeechRate(0.85f)
                tts?.setPitch(1.0f)
                val local = checkVoice()
                isReady = true
                // Default engine has no embedded Arabic voice? Look at the
                // other installed engines (once) before giving up.
                if (!local && enginePkg == null && !hunting) {
                    hunting = true
                    Thread({ huntEngineWithLocalArabic() }, "tts-engine-hunt").apply {
                        isDaemon = true
                        start()
                    }
                }
            } else {
                errorMessage = "موتور تبدیل متن به گفتار روی دستگاه یافت نشد"
            }
        }, enginePkg)
        tts = engine
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
            }

            override fun onDone(utteranceId: String?) {
                pos++
                speakCurrent()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                errorMessage = "خطا در پخش صوت"
                isSpeaking = false
            }
        })
    }

    /** Search installed TTS engines for one with an embedded Arabic voice. */
    private fun huntEngineWithLocalArabic() {
        try {
            val pkgs = try { tts?.engines?.map { it.name } } catch (_: Exception) { null }
                ?: return
            val defaultPkg = try { tts?.defaultEngine } catch (_: Exception) { null }
            for (pkg in pkgs) {
                if (pkg == defaultPkg) continue
                if (probeEngineForLocalArabic(pkg)) {
                    try { tts?.shutdown() } catch (_: Exception) { }
                    tts = null
                    isReady = false
                    initEngine(pkg)
                    return
                }
            }
        } catch (_: Exception) { /* keep current engine */ }
    }

    /** Returns true if the given engine package offers an offline Arabic voice. */
    private fun probeEngineForLocalArabic(pkg: String): Boolean {
        var probe: TextToSpeech? = null
        return try {
            val latch = CountDownLatch(1)
            var ok = TextToSpeech.ERROR
            probe = TextToSpeech(appContext, { status -> ok = status; latch.countDown() }, pkg)
            if (!latch.await(4, TimeUnit.SECONDS)) return false
            if (ok != TextToSpeech.SUCCESS) return false
            val t = probe ?: return false
            t.setLanguage(Locale("ar"))
            t.voices?.any { it.locale.language == "ar" && !it.isNetworkConnectionRequired } == true
        } catch (_: Exception) {
            false
        } finally {
            try { probe?.shutdown() } catch (_: Exception) { }
        }
    }

    /**
     * Evaluate Arabic voice availability on the current engine.
     * @return true if an on-device Arabic voice is now active.
     */
    private fun checkVoice(): Boolean {
        val engine = tts ?: return false
        var res = engine.setLanguage(Locale("ar"))
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            res = engine.setLanguage(Locale("ar", "SA"))
        }
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            needsVoiceData = true
            offlineReady = false
            errorMessage = "بسته صوتی عربی روی دستگاه نصب نیست؛ برای پخش کاملا آفلاین یک‌بار آن را نصب کنید"
            return false
        }
        return try {
            val localAr: List<Voice> = engine.voices
                ?.filter { it.locale.language == "ar" && !it.isNetworkConnectionRequired }
                ?.sortedByDescending { it.quality }
                ?: emptyList()
            if (localAr.isNotEmpty()) {
                engine.voice = localAr.first()
                offlineReady = true
                needsVoiceData = false
                errorMessage = null
                true
            } else {
                // Engine speaks Arabic only via network synthesis; keep it as a
                // fallback (works with internet) but say so honestly.
                offlineReady = false
                needsVoiceData = false
                errorMessage = "صدای آفلاین عربی یافت نشد؛ پخش فعلا به اینترنت نیاز دارد"
                false
            }
        } catch (_: Exception) {
            offlineReady = false
            needsVoiceData = false
            false
        }
    }

    /** Re-check voice availability (call when returning from the installer/settings). */
    fun refresh() {
        if (isReady) {
            try { checkVoice() } catch (_: Exception) { /* engine busy, keep old state */ }
        }
    }

    /** Locks synthesis to on-device — used only when an offline voice is active. */
    private fun offlineParams(): Bundle = Bundle().apply {
        putString(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS, "false")
    }

    private fun speakCurrent() {
        val engine = tts ?: return
        if (pos in verses.indices) {
            currentIndex = verseIndices[pos]
            val params = if (offlineReady) offlineParams() else null
            val r = engine.speak(verses[pos], TextToSpeech.QUEUE_FLUSH, params, "v_$pos")
            if (r == TextToSpeech.ERROR) {
                errorMessage = "پخش شروع نشد؛ اگر بسته صوتی عربی نصب نیست، دکمه نصب را بزنید"
                isSpeaking = false
            }
        } else {
            isSpeaking = false
            currentIndex = -1
        }
    }

    /** One-time download of the Arabic voice pack (after this, playback is offline). */
    fun installVoiceData(activity: Activity) {
        try {
            activity.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
            return
        } catch (_: Exception) { /* fall through to settings */ }
        try {
            activity.startActivity(Intent("com.android.settings.TTS_SETTINGS"))
        } catch (_: Exception) {
            errorMessage = "نصب خودکار ممکن نیست؛ در تنظیمات اندروید: زبان‌ها > تبدیل متن به گفتار > نصب بسته صوتی عربی"
        }
    }

    /** Start reciting from the beginning (or from the given verse id). */
    fun play(versesWithIndex: List<Pair<Int, String>>, startIndex: Int = 0) {
        if (!isReady) {
            if (errorMessage == null) errorMessage = "موتور تبدیل متن به گفتار آماده نیست؛ لحظه‌ای صبر کنید و دوباره بزنید"
            return
        }
        if (needsVoiceData) {
            errorMessage = "اول بسته صوتی عربی را با دکمه «نصب یک‌باره» نصب کنید، بعد پخش را بزنید"
            return
        }
        this.verses = versesWithIndex.map { it.second }
        this.verseIndices = versesWithIndex.map { it.first }
        val startPos = verseIndices.indexOf(startIndex).coerceAtLeast(0)
        pos = startPos
        isSpeaking = true
        speakCurrent()
    }

    /** Pause after the current verse finishes (keeps position). */
    fun pause() {
        tts?.stop()
        isSpeaking = false
    }

    /** Resume from where playback was paused. */
    fun resume() {
        if (verses.isNotEmpty() && pos in verses.indices) {
            isSpeaking = true
            speakCurrent()
        }
    }

    /** Stop and reset to the beginning. */
    fun stop() {
        tts?.stop()
        isSpeaking = false
        currentIndex = -1
        pos = 0
    }

    fun shutdown() {
        try { tts?.stop() } catch (_: Exception) { }
        try { tts?.shutdown() } catch (_: Exception) { }
        tts = null
    }
}

@Composable
fun rememberPrayerReciter(): PrayerReciter {
    val context = LocalContext.current
    val reciter = remember { PrayerReciter(context) }
    DisposableEffect(reciter) {
        onDispose { reciter.shutdown() }
    }
    return reciter
}
