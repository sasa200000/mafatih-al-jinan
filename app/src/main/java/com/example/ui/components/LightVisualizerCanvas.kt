package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.model.LightPalette
import com.example.model.LightShapeMode
import com.example.model.LightShowSettings
import com.example.ui.theme.LightColorCosmic
import com.example.ui.theme.LightColorCyanBreeze
import com.example.ui.theme.LightColorGoldAmber
import com.example.ui.theme.LightColorRainbow
import com.example.ui.theme.LightColorSpiritual
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LightVisualizerCanvas(
    settings: LightShowSettings,
    modifier: Modifier = Modifier
) {
    val durationMs = remember(settings.speedBpm) {
        // Map 30..180 BPM to cycle ms: 60,000 / BPM
        val bpm = settings.speedBpm.coerceIn(30f, 180f)
        (60000f / bpm).toInt().coerceIn(300, 2000)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "lightShowTransition")

    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulsePhase"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs * 4, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    val colors = remember(settings.palette) {
        when (settings.palette) {
            LightPalette.SPIRITUAL_GOLD -> LightColorSpiritual
            LightPalette.RAINBOW_NEON -> LightColorRainbow
            LightPalette.CYAN_AURORA -> LightColorCyanBreeze
            LightPalette.COSMIC_VIOLET -> LightColorCosmic
            LightPalette.WARM_AMBER -> LightColorGoldAmber
            LightPalette.DIAMOND_WHITE -> listOf(
                Color.White,
                Color(0xFFE0F7FA),
                Color(0xFFB2EBF2),
                Color(0xFF80DEEA),
                Color.White
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = minOf(size.width, size.height) * 0.45f

        // Ambient Background Glow
        val bgBrush = Brush.radialGradient(
            colors = listOf(
                colors[0].copy(alpha = 0.25f * settings.brightnessMultiplier),
                colors[1 % colors.size].copy(alpha = 0.10f * settings.brightnessMultiplier),
                Color.Transparent
            ),
            center = center,
            radius = maxRadius * 1.5f
        )
        drawCircle(brush = bgBrush, radius = maxRadius * 1.5f, center = center)

        when (settings.shapeMode) {
            LightShapeMode.MANDALA_STAR -> {
                drawMandalaStar(center, maxRadius, pulsePhase, rotationAngle, colors, settings.brightnessMultiplier)
            }
            LightShapeMode.CRESCENT_GALAXY -> {
                drawCrescentGalaxy(center, maxRadius, pulsePhase, rotationAngle, colors, settings.brightnessMultiplier)
            }
            LightShapeMode.SACRED_GEOMETRY -> {
                drawSacredGeometry(center, maxRadius, pulsePhase, rotationAngle, colors, settings.brightnessMultiplier)
            }
            LightShapeMode.AURORA_WAVES -> {
                drawAuroraWaves(size, center, maxRadius, pulsePhase, colors, settings.brightnessMultiplier)
            }
            LightShapeMode.KALEIDOSCOPE_LOTUS -> {
                drawKaleidoscopeLotus(center, maxRadius, pulsePhase, rotationAngle, colors, settings.brightnessMultiplier)
            }
            LightShapeMode.DISCO_STROBE -> {
                drawDiscoStrobe(size, center, maxRadius, pulsePhase, rotationAngle, colors, settings.brightnessMultiplier)
            }
            LightShapeMode.PULSE_HEART -> {
                drawPulseHeart(center, maxRadius, pulsePhase, colors, settings.brightnessMultiplier)
            }
        }
    }
}

// 1. Mandala & Islamic Star
private fun DrawScope.drawMandalaStar(
    center: Offset,
    maxRadius: Float,
    pulse: Float,
    rotation: Float,
    colors: List<Color>,
    brightness: Float
) {
    val pulseFactor = 0.85f + 0.3f * sin(pulse * 2 * PI).toFloat()

    // 12-point outer rays
    rotate(rotation, center) {
        val numRays = 12
        for (i in 0 until numRays) {
            val angleRad = (i * 360f / numRays) * (PI / 180f)
            val rayLength = maxRadius * pulseFactor * (0.8f + 0.2f * (i % 2))
            val endX = center.x + rayLength * cos(angleRad).toFloat()
            val endY = center.y + rayLength * sin(angleRad).toFloat()
            val rayColor = colors[i % colors.size].copy(alpha = 0.75f * brightness)

            drawLine(
                color = rayColor,
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 4f * brightness,
                cap = StrokeCap.Round
            )

            // Orbiting glow beads
            drawCircle(
                color = colors[(i + 1) % colors.size].copy(alpha = 0.9f * brightness),
                radius = 8f * pulseFactor * brightness,
                center = Offset(endX, endY)
            )
        }
    }

    // Rotating 8-point geometric star
    rotate(-rotation * 1.5f, center) {
        val starPath = Path()
        val numPoints = 8
        val outerR = maxRadius * 0.65f * pulseFactor
        val innerR = outerR * 0.45f

        for (i in 0 until numPoints * 2) {
            val r = if (i % 2 == 0) outerR else innerR
            val a = (i * PI / numPoints)
            val x = center.x + r * cos(a).toFloat()
            val y = center.y + r * sin(a).toFloat()
            if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
        }
        starPath.close()

        drawPath(
            path = starPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    colors[0].copy(alpha = 0.6f * brightness),
                    colors[2 % colors.size].copy(alpha = 0.2f * brightness)
                ),
                center = center,
                radius = outerR
            ),
            style = Fill
        )

        drawPath(
            path = starPath,
            color = colors[1 % colors.size].copy(alpha = 0.9f * brightness),
            style = Stroke(width = 3.5f * brightness)
        )
    }

    // Pulsing central sun
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f * brightness),
                colors[0].copy(alpha = 0.8f * brightness),
                Color.Transparent
            ),
            center = center,
            radius = maxRadius * 0.35f * pulseFactor
        ),
        radius = maxRadius * 0.35f * pulseFactor,
        center = center
    )
}

// 2. Crescent & Galaxy
private fun DrawScope.drawCrescentGalaxy(
    center: Offset,
    maxRadius: Float,
    pulse: Float,
    rotation: Float,
    colors: List<Color>,
    brightness: Float
) {
    val pulseFactor = 0.9f + 0.25f * sin(pulse * 2 * PI).toFloat()

    // Orbiting celestial particles
    rotate(rotation * 0.8f, center) {
        val particleCount = 28
        for (i in 0 until particleCount) {
            val angle = (i * 360f / particleCount) * (PI / 180f)
            val r = maxRadius * (0.3f + 0.65f * ((i * 17 % 10) / 10f))
            val px = center.x + r * cos(angle + pulse * 0.5).toFloat()
            val py = center.y + r * sin(angle + pulse * 0.5).toFloat()
            val pSize = (3f + (i % 5) * 2f) * pulseFactor * brightness

            drawCircle(
                color = colors[i % colors.size].copy(alpha = (0.4f + 0.5f * sin(pulse * 3f + i).toFloat().coerceIn(0f, 1f)) * brightness),
                radius = pSize,
                center = Offset(px, py)
            )
        }
    }

    // Glowing Crescent Moon
    val crescentR = maxRadius * 0.55f * pulseFactor
    val moonPath = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(center.x - crescentR, center.y - crescentR, center.x + crescentR, center.y + crescentR))
    }

    val moonCutPath = Path().apply {
        val offsetCut = crescentR * 0.35f
        addOval(androidx.compose.ui.geometry.Rect(center.x - crescentR + offsetCut, center.y - crescentR - offsetCut * 0.2f, center.x + crescentR + offsetCut, center.y + crescentR - offsetCut * 0.2f))
    }

    drawPath(
        path = moonPath,
        brush = Brush.linearGradient(
            colors = listOf(
                colors[0].copy(alpha = 0.9f * brightness),
                colors[1 % colors.size].copy(alpha = 0.7f * brightness)
            ),
            start = Offset(center.x - crescentR, center.y - crescentR),
            end = Offset(center.x + crescentR, center.y + crescentR)
        ),
        style = Stroke(width = crescentR * 0.35f, cap = StrokeCap.Round)
    )

    // Radiant Star inside Crescent
    rotate(rotation * 2f, Offset(center.x + crescentR * 0.3f, center.y - crescentR * 0.2f)) {
        val starCenter = Offset(center.x + crescentR * 0.3f, center.y - crescentR * 0.2f)
        val starR = maxRadius * 0.22f * pulseFactor

        for (i in 0 until 4) {
            val a = i * PI / 2f
            drawLine(
                color = Color.White.copy(alpha = 0.95f * brightness),
                start = Offset(starCenter.x - starR * cos(a).toFloat(), starCenter.y - starR * sin(a).toFloat()),
                end = Offset(starCenter.x + starR * cos(a).toFloat(), starCenter.y + starR * sin(a).toFloat()),
                strokeWidth = 3f * brightness,
                cap = StrokeCap.Round
            )
        }
        drawCircle(
            color = colors[0].copy(alpha = 0.9f * brightness),
            radius = 8f * pulseFactor * brightness,
            center = starCenter
        )
    }
}

// 3. Sacred Geometry (Neon Concentric Rings & Polygons)
private fun DrawScope.drawSacredGeometry(
    center: Offset,
    maxRadius: Float,
    pulse: Float,
    rotation: Float,
    colors: List<Color>,
    brightness: Float
) {
    val rings = 5
    for (i in 1..rings) {
        val fraction = (i + pulse) % rings / rings.toFloat()
        val r = maxRadius * fraction
        val color = colors[(i - 1) % colors.size].copy(alpha = (1f - fraction) * 0.85f * brightness)
        val strokeW = (4f * (1f - fraction) + 1.5f) * brightness

        // Alternating Rotating Polygons: Triangle, Hexagon, Octagon
        val sides = when (i % 3) {
            0 -> 6
            1 -> 8
            else -> 4
        }

        rotate(if (i % 2 == 0) rotation * (i * 0.4f) else -rotation * (i * 0.4f), center) {
            val polyPath = Path()
            for (s in 0 until sides) {
                val a = (s * 2 * PI / sides)
                val x = center.x + r * cos(a).toFloat()
                val y = center.y + r * sin(a).toFloat()
                if (s == 0) polyPath.moveTo(x, y) else polyPath.lineTo(x, y)
            }
            polyPath.close()

            drawPath(path = polyPath, color = color, style = Stroke(width = strokeW))

            // Circles around corners
            for (s in 0 until sides) {
                val a = (s * 2 * PI / sides)
                val x = center.x + r * cos(a).toFloat()
                val y = center.y + r * sin(a).toFloat()
                drawCircle(color = color, radius = 4f * brightness, center = Offset(x, y))
            }
        }
    }
}

// 4. Aurora Waves
private fun DrawScope.drawAuroraWaves(
    size: Size,
    center: Offset,
    maxRadius: Float,
    pulse: Float,
    colors: List<Color>,
    brightness: Float
) {
    val waveCount = 5
    for (w in 0 until waveCount) {
        val color = colors[w % colors.size].copy(alpha = (0.5f + 0.3f * sin((pulse + w) * PI).toFloat()) * brightness)
        val path = Path()
        val yBase = center.y - maxRadius * 0.6f + w * (maxRadius * 1.2f / waveCount)
        val amplitude = 40f + 25f * sin((pulse * 2 * PI + w)).toFloat()
        val frequency = 0.008f + w * 0.002f

        path.moveTo(0f, yBase)
        var x = 0f
        while (x <= size.width) {
            val y = yBase + amplitude * sin((x * frequency + pulse * 2 * PI + w)).toFloat()
            path.lineTo(x, y)
            x += 10f
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = (4f + w * 1.5f) * brightness, cap = StrokeCap.Round)
        )
    }

    // Central radiant equalizer columns
    val bars = 16
    val barWidth = size.width / (bars * 2.5f)
    for (b in 0 until bars) {
        val x = (center.x - (bars * barWidth * 1.2f) / 2f) + b * barWidth * 1.2f
        val heightMultiplier = (sin(pulse * 4 * PI + b * 0.5f).toFloat().coerceIn(-1f, 1f) + 1.2f) * 0.5f
        val barH = maxRadius * 0.7f * heightMultiplier
        val barColor = colors[b % colors.size].copy(alpha = 0.8f * brightness)

        drawLine(
            color = barColor,
            start = Offset(x, center.y + barH / 2f),
            end = Offset(x, center.y - barH / 2f),
            strokeWidth = barWidth * 0.75f,
            cap = StrokeCap.Round
        )
    }
}

// 5. Kaleidoscope Lotus
private fun DrawScope.drawKaleidoscopeLotus(
    center: Offset,
    maxRadius: Float,
    pulse: Float,
    rotation: Float,
    colors: List<Color>,
    brightness: Float
) {
    val pulseFactor = 0.8f + 0.3f * sin(pulse * 2 * PI).toFloat()
    val petalLayers = 3
    val petalsPerLayer = 8

    for (layer in 0 until petalLayers) {
        val layerR = maxRadius * (0.4f + layer * 0.25f) * pulseFactor
        val layerColor = colors[layer % colors.size].copy(alpha = (0.7f - layer * 0.15f) * brightness)
        val layerRotation = if (layer % 2 == 0) rotation + layer * 15f else -rotation - layer * 15f

        rotate(layerRotation, center) {
            for (p in 0 until petalsPerLayer) {
                val a = p * 2 * PI / petalsPerLayer
                val petalCenter = Offset(
                    center.x + (layerR * 0.5f) * cos(a).toFloat(),
                    center.y + (layerR * 0.5f) * sin(a).toFloat()
                )

                drawOval(
                    color = layerColor,
                    topLeft = Offset(petalCenter.x - layerR * 0.25f, petalCenter.y - layerR * 0.5f),
                    size = Size(layerR * 0.5f, layerR),
                    style = Stroke(width = 3f * brightness)
                )

                drawCircle(
                    color = colors[(p + layer) % colors.size].copy(alpha = 0.85f * brightness),
                    radius = 5f * brightness,
                    center = Offset(
                        center.x + layerR * cos(a).toFloat(),
                        center.y + layerR * sin(a).toFloat()
                    )
                )
            }
        }
    }

    // Glowing core
    drawCircle(
        color = Color.White.copy(alpha = 0.95f * brightness),
        radius = 16f * pulseFactor * brightness,
        center = center
    )
}

// 6. Disco Strobe & Lasers
private fun DrawScope.drawDiscoStrobe(
    size: Size,
    center: Offset,
    maxRadius: Float,
    pulse: Float,
    rotation: Float,
    colors: List<Color>,
    brightness: Float
) {
    val isFlashBeat = (pulse > 0.85f)
    val flashColor = if (isFlashBeat) Color.White.copy(alpha = 0.4f * brightness) else Color.Transparent

    // Instant strobe flash on beat
    drawRect(color = flashColor, topLeft = Offset.Zero, size = size)

    // 8 Crossing Laser beams
    rotate(rotation * 2.5f, center) {
        val laserCount = 8
        for (i in 0 until laserCount) {
            val angle = (i * 360f / laserCount) * (PI / 180f)
            val endX = center.x + maxRadius * 2.5f * cos(angle).toFloat()
            val endY = center.y + maxRadius * 2.5f * sin(angle).toFloat()
            val laserColor = colors[i % colors.size].copy(alpha = 0.85f * brightness)

            drawLine(
                color = laserColor,
                start = center,
                end = Offset(endX, endY),
                strokeWidth = (5f + if (isFlashBeat) 6f else 0f) * brightness,
                cap = StrokeCap.Round
            )
        }
    }

    // Mirror ball disco facets
    rotate(-rotation, center) {
        val facetR = maxRadius * 0.45f
        for (fx in -3..3) {
            for (fy in -3..3) {
                val ox = center.x + fx * (facetR * 0.28f)
                val oy = center.y + fy * (facetR * 0.28f)
                val dist = kotlin.math.hypot(ox - center.x, oy - center.y)
                if (dist < facetR) {
                    val facetColor = colors[(fx + fy + 10) % colors.size].copy(alpha = (0.6f + 0.3f * sin(pulse * 6f + fx + fy).toFloat()) * brightness)
                    drawCircle(color = facetColor, radius = 6f * brightness, center = Offset(ox, oy))
                }
            }
        }
        drawCircle(
            color = colors[0].copy(alpha = 0.8f * brightness),
            radius = facetR,
            center = center,
            style = Stroke(width = 3f * brightness)
        )
    }
}

// 7. Pulse Heart & Sacred Glow
private fun DrawScope.drawPulseHeart(
    center: Offset,
    maxRadius: Float,
    pulse: Float,
    colors: List<Color>,
    brightness: Float
) {
    // Cardiac-style double beat: lub-dub
    val beatFactor = if (pulse < 0.2f) {
        1f + 0.35f * sin(pulse / 0.2f * PI).toFloat()
    } else if (pulse in 0.25f..0.45f) {
        1f + 0.2f * sin((pulse - 0.25f) / 0.2f * PI).toFloat()
    } else {
        1f
    }

    // Halo waves emitting outward
    val ripples = 4
    for (r in 0 until ripples) {
        val fraction = (pulse + r.toFloat() / ripples) % 1f
        val rippleR = maxRadius * fraction * 1.3f
        val alpha = (1f - fraction) * 0.7f * brightness
        drawCircle(
            color = colors[r % colors.size].copy(alpha = alpha),
            radius = rippleR,
            center = center,
            style = Stroke(width = (4f * (1f - fraction) + 1.5f) * brightness)
        )
    }

    // Glowing Sacred Heart Path
    val heartR = maxRadius * 0.45f * beatFactor
    val heartPath = Path().apply {
        moveTo(center.x, center.y + heartR * 0.6f)
        cubicTo(
            center.x - heartR * 1.2f, center.y - heartR * 0.2f,
            center.x - heartR * 0.7f, center.y - heartR * 1.1f,
            center.x, center.y - heartR * 0.4f
        )
        cubicTo(
            center.x + heartR * 0.7f, center.y - heartR * 1.1f,
            center.x + heartR * 1.2f, center.y - heartR * 0.2f,
            center.x, center.y + heartR * 0.6f
        )
        close()
    }

    drawPath(
        path = heartPath,
        brush = Brush.radialGradient(
            colors = listOf(
                colors[0].copy(alpha = 0.95f * brightness),
                colors[1 % colors.size].copy(alpha = 0.8f * brightness),
                colors[2 % colors.size].copy(alpha = 0.3f * brightness)
            ),
            center = center,
            radius = heartR
        ),
        style = Fill
    )

    drawPath(
        path = heartPath,
        color = Color.White.copy(alpha = 0.9f * brightness),
        style = Stroke(width = 3.5f * brightness)
    )
}
