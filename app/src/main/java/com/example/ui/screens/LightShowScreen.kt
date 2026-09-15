package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BorderOuter
import androidx.compose.material.icons.filled.CenterFocusStrong
import com.example.model.BorderLightSettings
import com.example.model.BorderLightShape
import com.example.model.LightPalette
import com.example.model.LightShapeMode
import com.example.model.LightShowSettings
import com.example.ui.components.LightVisualizerCanvas
import com.example.ui.components.ScreenEdgeLighting
import com.example.ui.components.SupportHeaderButton
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TurquoiseNeon
import com.example.utils.FlashlightHelper
import com.example.utils.VibrationHelper
import kotlinx.coroutines.delay

enum class LightDisplayTarget(val titleFa: String) {
    COMBO("ترکیب مرکز + دور صفحه"),
    BORDER_ONLY("رقص نور دور صفحه"),
    CENTER_ONLY("رقص نور مرکزی")
}

@Composable
fun LightShowScreen(
    borderSettings: BorderLightSettings,
    onUpdateBorderSettings: (BorderLightSettings) -> Unit,
    onOpenSupport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val flashlightHelper = remember { FlashlightHelper(context) }
    val vibrationHelper = remember { VibrationHelper(context) }

    var displayTarget by remember { mutableStateOf(LightDisplayTarget.COMBO) }
    var activeConfigTab by remember { mutableStateOf(0) } // 0: Border Light, 1: Center Light

    var selectedShape by remember { mutableStateOf(LightShapeMode.MANDALA_STAR) }
    var selectedPalette by remember { mutableStateOf(LightPalette.SPIRITUAL_GOLD) }
    var speedBpm by remember { mutableFloatStateOf(65f) }
    var isPlaying by remember { mutableStateOf(true) }
    var isFlashlightEnabled by remember { mutableStateOf(false) }
    var isVibrationEnabled by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    // Flashlight & Vibration rhythm pulse loop
    LaunchedEffect(isPlaying, isFlashlightEnabled, isVibrationEnabled, speedBpm) {
        if (!isPlaying || (!isFlashlightEnabled && !isVibrationEnabled)) {
            flashlightHelper.setTorchMode(false)
            return@LaunchedEffect
        }

        val intervalMs = (60000L / speedBpm.toLong().coerceIn(30, 180)).coerceIn(300, 2000)

        while (isPlaying && (isFlashlightEnabled || isVibrationEnabled)) {
            if (isFlashlightEnabled && flashlightHelper.isFlashSupported) {
                flashlightHelper.setTorchMode(true)
            }
            if (isVibrationEnabled) {
                vibrationHelper.vibrateShort()
            }
            delay(60)
            if (isFlashlightEnabled && flashlightHelper.isFlashSupported) {
                flashlightHelper.setTorchMode(false)
            }
            delay(intervalMs - 60)
        }
    }

    // Safely turn off torch when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            flashlightHelper.setTorchMode(false)
        }
    }

    val settings = LightShowSettings(
        shapeMode = selectedShape,
        palette = selectedPalette,
        speedBpm = speedBpm,
        isFlashlightEnabled = isFlashlightEnabled,
        isVibrationEnabled = isVibrationEnabled,
        isPlaying = isPlaying,
        brightnessMultiplier = 1.0f
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030907))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isFullscreen) {
                    isFullscreen = false
                }
            }
            .testTag("light_show_screen_container")
    ) {
        // 1. Center Visualizer Canvas (if active)
        if (displayTarget != LightDisplayTarget.BORDER_ONLY) {
            LightVisualizerCanvas(
                settings = settings,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Multi-Shape Edge Lighting around perimeter of the screen (دور تا دور صفحه)
        if (displayTarget != LightDisplayTarget.CENTER_ONLY && borderSettings.isEnabled && isPlaying) {
            ScreenEdgeLighting(
                settings = borderSettings,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top Controls Bar
        AnimatedVisibility(
            visible = !isFullscreen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SupportHeaderButton(onClick = onOpenSupport)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Border Light Toggle
                    Surface(
                        onClick = {
                            onUpdateBorderSettings(borderSettings.copy(isEnabled = !borderSettings.isEnabled))
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (borderSettings.isEnabled) EmeraldPrimary else EmeraldDark.copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (borderSettings.isEnabled) TurquoiseNeon else Color.Gray.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("quick_border_light_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BorderOuter,
                                contentDescription = null,
                                tint = if (borderSettings.isEnabled) TurquoiseNeon else Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (borderSettings.isEnabled) "نور دور صفحه: روشن" else "نور دور صفحه: خاموش",
                                fontSize = 11.sp,
                                color = if (borderSettings.isEnabled) GoldAccent else Color.LightGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Play / Pause
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EmeraldDark.copy(alpha = 0.85f))
                            .testTag("light_show_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "توقف" else "شروع",
                            tint = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Fullscreen
                    IconButton(
                        onClick = { isFullscreen = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EmeraldDark.copy(alpha = 0.85f))
                            .testTag("light_show_fullscreen_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "تمام صفحه",
                            tint = TurquoiseNeon
                        )
                    }
                }
            }
        }

        // Bottom Controls Panel
        AnimatedVisibility(
            visible = !isFullscreen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 75.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = EmeraldDark.copy(alpha = 0.94f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .testTag("light_show_controls_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Target Mode Selector (Combo / Border / Center)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LightDisplayTarget.values().forEach { target ->
                            val isSel = displayTarget == target
                            Surface(
                                onClick = { displayTarget = target },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) EmeraldPrimary else Color.Transparent,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = target.titleFa,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Config Tab Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            onClick = { activeConfigTab = 0 },
                            shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                            color = if (activeConfigTab == 0) TurquoiseNeon.copy(alpha = 0.25f) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (activeConfigTab == 0) TurquoiseNeon else Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BorderOuter,
                                    contentDescription = null,
                                    tint = TurquoiseNeon,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "رقص نور دور تا دور صفحه",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeConfigTab == 0) TurquoiseNeon else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            onClick = { activeConfigTab = 1 },
                            shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                            color = if (activeConfigTab == 1) GoldAccent.copy(alpha = 0.25f) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (activeConfigTab == 1) GoldAccent else Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CenterFocusStrong,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "رقص نور مرکزی",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeConfigTab == 1) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (activeConfigTab == 0) {
                        // BORDER LIGHT SETTINGS (دور تا دور صفحه)
                        Text(
                            text = "شکل رقص نور دور صفحه: ${borderSettings.shape.titleFa}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TurquoiseNeon
                        )
                        Text(
                            text = borderSettings.shape.description,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Border Shapes list
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(BorderLightShape.values()) { shape ->
                                FilterChip(
                                    selected = borderSettings.shape == shape,
                                    onClick = { onUpdateBorderSettings(borderSettings.copy(shape = shape, isEnabled = true)) },
                                    label = { Text(shape.titleFa, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldPrimary,
                                        selectedLabelColor = TurquoiseNeon
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Border Palette
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(LightPalette.values()) { pal ->
                                FilterChip(
                                    selected = borderSettings.palette == pal,
                                    onClick = { onUpdateBorderSettings(borderSettings.copy(palette = pal)) },
                                    label = { Text(pal.titleFa, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldPrimary,
                                        selectedLabelColor = GoldAccent
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Border Width and Global Toggle Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ضخامت نور: ${borderSettings.strokeWidthDp.toInt()} dp",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(4f to "باریک", 8f to "استاندارد", 14f to "پهن").forEach { (widthVal, label) ->
                                    Surface(
                                        onClick = { onUpdateBorderSettings(borderSettings.copy(strokeWidthDp = widthVal)) },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (borderSettings.strokeWidthDp == widthVal) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 9.sp,
                                            color = if (borderSettings.strokeWidthDp == widthVal) TurquoiseNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Global Full-App Edge Lighting Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "نمایش نور دور صفحه در تمام برنامه (دعا و ذکر)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Switch(
                                checked = borderSettings.isGlobalEnabled,
                                onCheckedChange = {
                                    onUpdateBorderSettings(borderSettings.copy(isGlobalEnabled = it, isEnabled = true))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GoldAccent,
                                    checkedTrackColor = EmeraldPrimary
                                )
                            )
                        }

                    } else {
                        // CENTER VISUALIZER SETTINGS
                        Text(
                            text = "شکل رقص نور مرکزی: ${selectedShape.titleFa}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Text(
                            text = selectedShape.description,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(LightShapeMode.values()) { shape ->
                                FilterChip(
                                    selected = selectedShape == shape,
                                    onClick = { selectedShape = shape },
                                    label = { Text(shape.titleFa, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldPrimary,
                                        selectedLabelColor = GoldAccent
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(LightPalette.values()) { pal ->
                                FilterChip(
                                    selected = selectedPalette == pal,
                                    onClick = { selectedPalette = pal },
                                    label = { Text(pal.titleFa, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldPrimary,
                                        selectedLabelColor = GoldAccent
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Flashlight and Vibration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = if (isFlashlightEnabled) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "فلش گوشی",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Switch(
                                    checked = isFlashlightEnabled,
                                    onCheckedChange = {
                                        if (it && !flashlightHelper.isFlashSupported) {
                                            Toast.makeText(context, "فلش دوربین در دسترس نیست", Toast.LENGTH_SHORT).show()
                                        }
                                        isFlashlightEnabled = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = GoldAccent,
                                        checkedTrackColor = EmeraldPrimary
                                    )
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = if (isVibrationEnabled) TurquoiseNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تپش هپتیک",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Switch(
                                    checked = isVibrationEnabled,
                                    onCheckedChange = { isVibrationEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = TurquoiseNeon,
                                        checkedTrackColor = EmeraldPrimary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Shared Speed Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سرعت ریتم: ${speedBpm.toInt()} BPM",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(45f to "آرام", 75f to "ملایم", 120f to "تند").forEach { (bpm, label) ->
                                Surface(
                                    onClick = {
                                        speedBpm = bpm
                                        onUpdateBorderSettings(borderSettings.copy(speedBpm = bpm))
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (speedBpm == bpm) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        color = if (speedBpm == bpm) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Slider(
                        value = speedBpm,
                        onValueChange = {
                            speedBpm = it
                            onUpdateBorderSettings(borderSettings.copy(speedBpm = it))
                        },
                        valueRange = 30f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldAccent,
                            activeTrackColor = EmeraldPrimary
                        )
                    )
                }
            }
        }


        // Fullscreen exit indicator tip
        if (isFullscreen) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "برای نمایش منو، روی صفحه ضربه بزنید",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
