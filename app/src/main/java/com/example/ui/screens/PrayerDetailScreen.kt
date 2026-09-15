package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BorderLightSettings
import com.example.model.BorderLightShape
import com.example.model.LightPalette
import com.example.model.LightShapeMode
import com.example.model.LightShowSettings
import com.example.model.PrayerItem
import com.example.model.PrayerVerse
import com.example.ui.components.LightVisualizerCanvas
import com.example.ui.components.ScreenEdgeLighting
import com.example.ui.components.SupportHeaderButton
import com.example.utils.PrayerReciter
import com.example.utils.VibrationHelper
import com.example.utils.rememberPrayerReciter
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.TextArabicGold
import com.example.ui.theme.TurquoiseNeon

import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerDetailScreen(
    prayer: PrayerItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
    onOpenSupport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showTranslation by remember { mutableStateOf(true) }
    var showFontSizeControls by remember { mutableStateOf(false) }
    var showAmbientLight by remember { mutableStateOf(false) }
    var arabicFontSize by remember { mutableFloatStateOf(20f) }
    var ambientShape by remember { mutableStateOf(LightShapeMode.MANDALA_STAR) }
    var borderLightShape by remember { mutableStateOf(BorderLightShape.FLOWING_NEON) }

    val verseCounters = remember { mutableStateMapOf<Int, Int>() }

    val reciter = rememberPrayerReciter()
    val verseAudioList = remember(prayer) {
        prayer.verses.map { it.id to it.arabic }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = prayer.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Text(
                            text = prayer.category.titleFa,
                            fontSize = 11.sp,
                            color = TurquoiseNeon
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = GoldAccent
                        )
                    }
                },
                actions = {
                    // Ambient Light Toggle
                    IconButton(
                        onClick = { showAmbientLight = !showAmbientLight },
                        modifier = Modifier.testTag("detail_ambient_light_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "رقص نور پس‌زمینه",
                            tint = if (showAmbientLight) TurquoiseNeon else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Font Size Toggle
                    IconButton(
                        onClick = { showFontSizeControls = !showFontSizeControls },
                        modifier = Modifier.testTag("detail_font_size_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "اندازه قلم",
                            tint = if (showFontSizeControls) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Translation Toggle
                    IconButton(
                        onClick = { showTranslation = !showTranslation },
                        modifier = Modifier.testTag("detail_translation_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "ترجمه فارسی",
                            tint = if (showTranslation) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Favorite Button
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.testTag("detail_favorite_button")) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "نشان کردن",
                            tint = if (isFavorite) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldDark
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Ambient Background Light Show if enabled
            if (showAmbientLight) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LightVisualizerCanvas(
                        settings = LightShowSettings(
                            shapeMode = ambientShape,
                            palette = LightPalette.SPIRITUAL_GOLD,
                            speedBpm = 45f,
                            brightnessMultiplier = 0.35f
                        )
                    )
                    // Dark scrim for readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.65f))
                    )
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {

                // Offline audio reciter bar (real TextToSpeech, no network, no assets)
                AudioPlayerBar(
                    reciter = reciter,
                    isSpeaking = reciter.isSpeaking,
                    currentIndex = reciter.currentIndex,
                    onPlay = { reciter.play(verseAudioList) },
                    onPause = { reciter.pause() },
                    onStop = { reciter.stop() },
                    errorMessage = reciter.errorMessage
                )

                // Font Size Adjuster Panel
                AnimatedVisibility(visible = showFontSizeControls) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "اندازه قلم متن عربی: ${arabicFontSize.toInt()}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "بزرگنمایی",
                                    fontSize = 13.sp,
                                    color = GoldAccent
                                )
                            }
                            Slider(
                                value = arabicFontSize,
                                onValueChange = { arabicFontSize = it },
                                valueRange = 16f..32f,
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldAccent,
                                    activeTrackColor = EmeraldPrimary
                                )
                            )

                            // Ambient Shape picker if ambient light is on
                            if (showAmbientLight) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "شکل رقص نور پس‌زمینه:",
                                    fontSize = 12.sp,
                                    color = TurquoiseNeon
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val shapes = listOf(
                                        LightShapeMode.MANDALA_STAR to "شمس",
                                        LightShapeMode.CRESCENT_GALAXY to "هلال",
                                        LightShapeMode.AURORA_WAVES to "آرورا",
                                        LightShapeMode.KALEIDOSCOPE_LOTUS to "نیلوفر"
                                    )
                                    shapes.forEach { (mode, label) ->
                                        FilterChip(
                                            selected = ambientShape == mode,
                                            onClick = { ambientShape = mode },
                                            label = { Text(label, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EmeraldPrimary,
                                                selectedLabelColor = GoldAccent
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "شکل رقص نور دور تا دور صفحه:",
                                    fontSize = 12.sp,
                                    color = TurquoiseNeon
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    BorderLightShape.values().forEach { shape ->
                                        FilterChip(
                                            selected = borderLightShape == shape,
                                            onClick = { borderLightShape = shape },
                                            label = { Text(shape.titleFa, fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EmeraldPrimary,
                                                selectedLabelColor = TurquoiseNeon
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Verses List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("prayer_verses_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Virtue Card
                    if (prayer.virtue.isNotBlank()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.25f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "فضیلت و ثواب",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = GoldAccent
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = prayer.virtue,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Verse Cards
                    items(prayer.verses, key = { it.id }) { verse ->
                        val count = verseCounters[verse.id] ?: 0
                        VerseCard(
                            verse = verse,
                            count = count,
                            showTranslation = showTranslation,
                            fontSize = arabicFontSize.sp,
                            onIncrementCount = {
                                verseCounters[verse.id] = count + 1
                            },
                            onResetCount = {
                                verseCounters[verse.id] = 0
                            }
                        )
                    }

                    // Support Prompt at Bottom
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenSupport() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "درخواست افزودن ادعیه دیگر و ارتباط با پشتیبان",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                SupportHeaderButton(onClick = onOpenSupport)
                            }
                        }
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }

            // Screen perimeter multi-shape edge lighting (دور تا دور صفحه)
            if (showAmbientLight) {
                ScreenEdgeLighting(
                    settings = BorderLightSettings(
                        isEnabled = true,
                        shape = borderLightShape,
                        palette = LightPalette.SPIRITUAL_GOLD,
                        strokeWidthDp = 5f,
                        speedBpm = 45f
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun VerseCard(
    verse: PrayerVerse,
    count: Int,
    showTranslation: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit,
    onIncrementCount: () -> Unit,
    onResetCount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember { VibrationHelper(context) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        ),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, GoldAccent.copy(alpha = 0.2f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("verse_card_${verse.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Verse Number and Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Verse index badge
                Surface(
                    shape = CircleShape,
                    color = EmeraldPrimary,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${verse.id}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Copy verse button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("Verse", "${verse.arabic}\n\n${verse.persian}")
                            clipboard?.setPrimaryClip(clip)
                            Toast.makeText(context, "متن کپی شد", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "کپی متن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic Text
            Text(
                text = verse.arabic,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                color = TextArabicGold,
                lineHeight = (fontSize.value * 1.6f).sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            // Persian Translation
            if (showTranslation && verse.persian.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = verse.persian,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Repeat Counter if specified (e.g. 100x Lan & Salam)
            if (verse.targetRepeats > 1 || verse.note != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldDark.copy(alpha = 0.85f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TurquoiseNeon.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            if (verse.note != null) {
                                Text(
                                    text = verse.note,
                                    fontSize = 11.sp,
                                    color = TurquoiseNeon,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "خوانده شده: $count از ${verse.targetRepeats}",
                                fontSize = 12.sp,
                                color = GoldLight
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                onClick = {
                                    onIncrementCount()
                                    vibrator.vibrateShort()
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(
                                    text = "+ شمارش",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioPlayerBar(
    reciter: PrayerReciter,
    isSpeaking: Boolean,
    currentIndex: Int,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldDark.copy(alpha = 0.92f)),
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Play / Pause toggle
                FloatingActionButton(
                    onClick = {
                        if (isSpeaking) onPause() else onPlay()
                    },
                    containerColor = EmeraldPrimary,
                    contentColor = GoldAccent,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isSpeaking) "توقف موقت پخش" else "پخش صوتی آفلاین",
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Stop
                FloatingActionButton(
                    onClick = onStop,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "توقف و بازگشت به اول",
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isSpeaking) "در حال پخش صوتی آفلاین..." else "پخش صوتی قرائت عربی",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Text(
                        text = if (isSpeaking && currentIndex >= 0)
                            "آیه ${currentIndex + 1} در حال پخش"
                        else "بدون اینترنت، با موتور تبدیل متن به گفتار دستگاه",
                        fontSize = 11.sp,
                        color = TurquoiseNeon
                    )
                }
            }

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = msg,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (reciter.needsVoiceData) {
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = {
                    (context as? android.app.Activity)?.let { reciter.installVoiceData(it) }
                }) {
                    Text(text = "نصب یک‌باره بسته صوتی عربی (بعد از آن کاملا آفلاین)", fontSize = 12.sp)
                }
            }
        }
    }
}
