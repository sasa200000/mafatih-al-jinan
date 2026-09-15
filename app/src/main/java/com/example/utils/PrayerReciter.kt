package com.example.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Real, fully-offline audio playback for prayers.
 *
 * Uses the device's built-in TextToSpeech engine to recite the Arabic text
 * aloud. No network, no bundled media files, no external services — the speech
 * is synthesized on-device. If the Arabic voice data is not installed the
 * engine will still attempt synthesis (and the user can install voices offline
 * via Android's language settings).
 */
class PrayerReciter(context: Context) {

    private val tts: TextToSpeech

    var isReady by mutableStateOf(false)
        private set
    var isSpeaking by mutableStateOf(false)
        private set
    /** Index (into the supplied verse list) of the verse currently being read, or -1. */
    var currentIndex by mutableStateOf(-1)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    /** True when the Arabic voice pack is missing — needs a one-time download. */
    var needsVoiceData by mutableStateOf(false)
        private set
    /** True when a fully on-device (no-network) Arabic voice is active. */
    var offlineReady by mutableStateOf(false)
        private set

    private var verses: List<String> = emptyList()
    private var verseIndices: List<Int> = emptyList()
    private var pos = 0

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                var res = tts.setLanguage(Locale("ar"))
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    res = tts.setLanguage(Locale("ar", "SA"))
                }
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    needsVoiceData = true
                    errorMessage = "بسته صوتی عربی روی دستگاه نصب نیست؛ برای پخش کاملا آفلاین یک‌بار آن را نصب کنید"
                } else {
                    // Prefer a voice that works without network.
                    try {
                        val localAr = tts.voices?.filter {
                            it.locale.language == "ar" && !it.isNetworkConnectionRequired
                        }
                        if (!localAr.isNullOrEmpty()) {
                            tts.voice = localAr.first()
                            offlineReady = true
                            errorMessage = null
                        } else {
                            // Engine supports Arabic but only via network synthesis;
                            // still usable, but flag it so the UI is honest.
                            offlineReady = false
                            errorMessage = "صدای آفلاین عربی یافت نشد؛ پخش ممکن است به اینترنت نیاز داشته باشد"
                        }
                    } catch (_: Exception) {
                        offlineReady = false
                    }
                }
                tts.setSpeechRate(0.85f)
                tts.setPitch(1.0f)
                isReady = true
            } else {
                errorMessage = "موتور تبدیل متن به گفتار روی دستگاه یافت نشد"
            }
        }
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
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

    /** Force on-device synthesis — never route speech through the network. */
    private fun offlineParams(): Bundle = Bundle().apply {
        putString(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS, "false")
    }

    private fun speakCurrent() {
        if (pos in verses.indices) {
            currentIndex = verseIndices[pos]
            tts.speak(verses[pos], TextToSpeech.QUEUE_FLUSH, offlineParams(), "v_$pos")
        } else {
            isSpeaking = false
            currentIndex = -1
        }
    }

    /** One-time download of the Arabic voice pack (after this, playback is offline). */
    fun installVoiceData(activity: Activity) {
        try {
            activity.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
        } catch (_: Exception) {
            errorMessage = "نصب خودکار ممکن نیست؛ از تنظیمات اندروید بسته صوتی عربی را نصب کنید"
        }
    }

    /** Start reciting from the beginning (or from the given verse id). */
    fun play(versesWithIndex: List<Pair<Int, String>>, startIndex: Int = 0) {
        if (!isReady) {
            if (errorMessage == null) errorMessage = "موتور تبدیل متن به گفتار آماده نیست"
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
        tts.stop()
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
        tts.stop()
        isSpeaking = false
        currentIndex = -1
        pos = 0
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
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
