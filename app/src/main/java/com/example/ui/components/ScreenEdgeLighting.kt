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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.BorderLightSettings
import com.example.model.BorderLightShape
import com.example.model.LightPalette
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.TurquoiseNeon
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ScreenEdgeLighting(
    settings: BorderLightSettings,
    modifier: Modifier = Modifier,
    cornerRadiusDp: Float = 28f
) {
    if (!settings.isEnabled) return

    val durationMs = (60000f / settings.speedBpm.coerceIn(30f, 180f) * 2f).toInt().coerceIn(600, 4000)

    val infiniteTransition = rememberInfiniteTransition(label = "edgeLightingAnim")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "edgeProgress"
    )

    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (durationMs / 2).coerceAtLeast(300), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "edgePulse"
    )

    val paletteColors = resolveBorderColors(settings.palette)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_edge_lighting_canvas")
    ) {
        val w = size.width
        val h = size.height
        val cornerRadiusPx = cornerRadiusDp.dp.toPx().coerceAtMost(w / 4f)
        val strokeWidthPx = settings.strokeWidthDp.dp.toPx()

        when (settings.shape) {
            BorderLightShape.FLOWING_NEON -> {
                drawFlowingNeonBorder(
                    w = w,
                    h = h,
                    r = cornerRadiusPx,
                    strokeWidth = strokeWidthPx,
                    progress = animProgress,
                    colors = paletteColors
                )
            }
            BorderLightShape.MANDALA_CORNERS -> {
                drawMandalaCornersBorder(
                    w = w,
                    h = h,
                    r = cornerRadiusPx,
                    strokeWidth = strokeWidthPx,
                    progress = animProgress,
                    pulse = pulsePhase,
                    colors = paletteColors
                )
            }
            BorderLightShape.RUNNING_PEARLS -> {
                drawRunningPearlsBorder(
                    w = w,
                    h = h,
                    r = cornerRadiusPx,
                    progress = animProgress,
                    colors = paletteColors,
                    strokeWidth = strokeWidthPx
                )
            }
            BorderLightShape.AURORA_FRAME -> {
                drawAuroraFrameBorder(
                    w = w,
                    h = h,
                    r = cornerRadiusPx,
                    pulse = pulsePhase,
                    progress = animProgress,
                    colors = paletteColors,
                    strokeWidth = strokeWidthPx
                )
            }
            BorderLightShape.CRESCENT_GEM -> {
                drawCrescentGemBorder(
                    w = w,
                    h = h,
                    r = cornerRadiusPx,
                    progress = animProgress,
                    pulse = pulsePhase,
                    colors = paletteColors,
                    strokeWidth = strokeWidthPx
                )
            }
            BorderLightShape.DISCO_STROBE_BORDER -> {
                drawDiscoStrobeBorder(
                    w = w,
                    h = h,
                    r = cornerRadiusPx,
                    progress = animProgress,
                    pulse = pulsePhase,
                    colors = paletteColors,
                    strokeWidth = strokeWidthPx
                )
            }
        }
    }
}

// Draw a sweeping, seamless neon laser stream around the rounded screen edge
private fun DrawScope.drawFlowingNeonBorder(
    w: Float,
    h: Float,
    r: Float,
    strokeWidth: Float,
    progress: Float,
    colors: List<Color>
) {
    val inset = strokeWidth / 2f
    val center = Offset(w / 2f, h / 2f)

    // Soft outer neon glow
    rotate(degrees = progress * 360f, pivot = center) {
        drawRoundRect(
            brush = Brush.sweepGradient(colors = colors, center = center),
            topLeft = Offset(inset, inset),
            size = Size(w - strokeWidth, h - strokeWidth),
            cornerRadius = CornerRadius(r, r),
            style = Stroke(width = strokeWidth * 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            alpha = 0.35f,
            blendMode = BlendMode.Screen
        )

        // Core sharp bright laser
        drawRoundRect(
            brush = Brush.sweepGradient(colors = colors, center = center),
            topLeft = Offset(inset, inset),
            size = Size(w - strokeWidth, h - strokeWidth),
            cornerRadius = CornerRadius(r, r),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
            alpha = 0.95f
        )
    }

    // Two traveling luminous comet heads on opposite sides
    val cometPos1 = getPerimeterPoint(progress, w - strokeWidth, h - strokeWidth, r)
    val cometPos2 = getPerimeterPoint((progress + 0.5f) % 1f, w - strokeWidth, h - strokeWidth, r)

    val offsetComet1 = Offset(cometPos1.x + inset, cometPos1.y + inset)
    val offsetComet2 = Offset(cometPos2.x + inset, cometPos2.y + inset)

    // Glowing comet beads
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, colors.first(), Color.Transparent),
            center = offsetComet1,
            radius = strokeWidth * 2.5f
        ),
        center = offsetComet1,
        radius = strokeWidth * 2.5f
    )
    drawCircle(color = Color.White, center = offsetComet1, radius = strokeWidth * 0.75f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, colors.getOrElse(colors.size / 2) { colors.first() }, Color.Transparent),
            center = offsetComet2,
            radius = strokeWidth * 2.5f
        ),
        center = offsetComet2,
        radius = strokeWidth * 2.5f
    )
    drawCircle(color = Color.White, center = offsetComet2, radius = strokeWidth * 0.75f)
}

// Mandala stars at 4 corners with connecting pulsing laser edges
private fun DrawScope.drawMandalaCornersBorder(
    w: Float,
    h: Float,
    r: Float,
    strokeWidth: Float,
    progress: Float,
    pulse: Float,
    colors: List<Color>
) {
    val inset = strokeWidth / 2f
    val cornerRadius = CornerRadius(r, r)

    // Connecting pulsing rounded border
    val borderAlpha = 0.5f + 0.4f * pulse
    drawRoundRect(
        brush = Brush.linearGradient(colors = colors, start = Offset(0f, 0f), end = Offset(w, h)),
        topLeft = Offset(inset, inset),
        size = Size(w - strokeWidth, h - strokeWidth),
        cornerRadius = cornerRadius,
        style = Stroke(width = strokeWidth * (0.8f + 0.4f * pulse), cap = StrokeCap.Round),
        alpha = borderAlpha
    )

    // 4 Corner Mandala Stars
    val cornerPositions = listOf(
        Offset(r + inset, r + inset),
        Offset(w - r - inset, r + inset),
        Offset(w - r - inset, h - r - inset),
        Offset(r + inset, h - r - inset)
    )

    val starRadius = (r * 0.75f).coerceIn(16f, 40f)

    cornerPositions.forEachIndexed { index, corner ->
        val rot = (progress * 360f * (if (index % 2 == 0) 1 else -1)) + (index * 45f)
        rotate(degrees = rot, pivot = corner) {
            // 8-point geometric star
            val path = Path()
            val points = 8
            val innerRad = starRadius * 0.45f
            val outerRad = starRadius * (0.9f + 0.2f * pulse)

            for (i in 0 until points * 2) {
                val angle = (i * PI / points).toFloat()
                val rad = if (i % 2 == 0) outerRad else innerRad
                val px = corner.x + rad * cos(angle)
                val py = corner.y + rad * sin(angle)
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()

            // Outer glow
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(colors.first().copy(alpha = 0.8f), Color.Transparent),
                    center = corner,
                    radius = outerRad * 1.5f
                ),
                style = Fill
            )

            // Star outline
            drawPath(
                path = path,
                color = colors[index % colors.size],
                style = Stroke(width = 2.5f)
            )

            // Center shining gem
            drawCircle(
                color = Color.White,
                center = corner,
                radius = 3.5f
            )
        }
    }
}

// Running luminous pearl beads traveling smoothly around the entire border
private fun DrawScope.drawRunningPearlsBorder(
    w: Float,
    h: Float,
    r: Float,
    progress: Float,
    colors: List<Color>,
    strokeWidth: Float
) {
    val inset = strokeWidth / 2f
    val beadCount = 32
    val beadRadius = strokeWidth * 0.75f

    // Subtle track line
    drawRoundRect(
        color = colors.first().copy(alpha = 0.18f),
        topLeft = Offset(inset, inset),
        size = Size(w - strokeWidth, h - strokeWidth),
        cornerRadius = CornerRadius(r, r),
        style = Stroke(width = 1.5f)
    )

    for (i in 0 until beadCount) {
        val beadProgress = (progress + (i.toFloat() / beadCount)) % 1f
        val pt = getPerimeterPoint(beadProgress, w - strokeWidth, h - strokeWidth, r)
        val pos = Offset(pt.x + inset, pt.y + inset)

        val col = colors[i % colors.size]
        val dynamicRadius = beadRadius * (0.8f + 0.3f * sin(progress * 2 * PI + i).toFloat())

        // Halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(col.copy(alpha = 0.7f), Color.Transparent),
                center = pos,
                radius = dynamicRadius * 2.2f
            ),
            center = pos,
            radius = dynamicRadius * 2.2f
        )

        // Solid bead
        drawCircle(
            color = col,
            center = pos,
            radius = dynamicRadius
        )

        // Core spark
        drawCircle(
            color = Color.White,
            center = pos,
            radius = dynamicRadius * 0.4f
        )
    }
}

// Deep breathing Aurora ambient frame that pulses inward
private fun DrawScope.drawAuroraFrameBorder(
    w: Float,
    h: Float,
    r: Float,
    pulse: Float,
    progress: Float,
    colors: List<Color>,
    strokeWidth: Float
) {
    val baseDepth = (strokeWidth * 2.5f).coerceIn(20f, 48f)
    val animatedDepth = baseDepth * (0.85f + 0.35f * pulse)

    val sweepCenter = Offset(w / 2f, h / 2f)

    rotate(degrees = progress * 180f, pivot = sweepCenter) {
        // Multi-layered inward glow
        listOf(1.0f to 0.25f, 0.6f to 0.45f, 0.3f to 0.75f).forEach { (depthFraction, alphaVal) ->
            val curStroke = animatedDepth * depthFraction
            val curInset = curStroke / 2f
            drawRoundRect(
                brush = Brush.sweepGradient(colors = colors, center = sweepCenter),
                topLeft = Offset(curInset, curInset),
                size = Size(w - curStroke, h - curStroke),
                cornerRadius = CornerRadius(r, r),
                style = Stroke(width = curStroke),
                alpha = alphaVal * (0.6f + 0.4f * pulse),
                blendMode = BlendMode.Screen
            )
        }
    }

    // Outer crisp perimeter line
    drawRoundRect(
        color = Color.White.copy(alpha = 0.8f),
        topLeft = Offset(1.5f, 1.5f),
        size = Size(w - 3f, h - 3f),
        cornerRadius = CornerRadius(r, r),
        style = Stroke(width = 2f)
    )
}

// Crescent moons and brilliant diamond sparks along border
private fun DrawScope.drawCrescentGemBorder(
    w: Float,
    h: Float,
    r: Float,
    progress: Float,
    pulse: Float,
    colors: List<Color>,
    strokeWidth: Float
) {
    val inset = strokeWidth / 2f

    // Perimeter line
    drawRoundRect(
        brush = Brush.linearGradient(colors = colors, start = Offset(0f, 0f), end = Offset(w, h)),
        topLeft = Offset(inset, inset),
        size = Size(w - strokeWidth, h - strokeWidth),
        cornerRadius = CornerRadius(r, r),
        style = Stroke(width = strokeWidth * 0.7f),
        alpha = 0.75f
    )

    // 4 Corner Crescents
    val cornerOffsets = listOf(
        Offset(r + inset, r + inset),
        Offset(w - r - inset, r + inset),
        Offset(w - r - inset, h - r - inset),
        Offset(r + inset, h - r - inset)
    )

    cornerOffsets.forEachIndexed { idx, corner ->
        val crescentRadius = (r * 0.65f).coerceIn(14f, 32f)
        val angle = (progress * 360f) + (idx * 90f)

        rotate(degrees = angle, pivot = corner) {
            // Crescent moon: outer circle minus shifted inner circle
            drawCircle(
                color = colors[idx % colors.size],
                center = corner,
                radius = crescentRadius
            )
            drawCircle(
                color = Color(0xFF030907),
                center = Offset(corner.x + crescentRadius * 0.4f, corner.y),
                radius = crescentRadius * 0.85f
            )

            // Star spark beside crescent
            val starX = corner.x - crescentRadius * 0.3f
            val starY = corner.y - crescentRadius * 0.3f
            drawCircle(
                color = Color.White,
                center = Offset(starX, starY),
                radius = 3f * (0.8f + 0.4f * pulse)
            )
        }
    }

    // 12 Traveling diamond gems
    val gemCount = 12
    for (i in 0 until gemCount) {
        val gemProg = (progress + (i.toFloat() / gemCount)) % 1f
        val pt = getPerimeterPoint(gemProg, w - strokeWidth, h - strokeWidth, r)
        val gemPos = Offset(pt.x + inset, pt.y + inset)

        val sizeGem = strokeWidth * (0.8f + 0.4f * pulse)
        // Draw diamond
        val diamondPath = Path().apply {
            moveTo(gemPos.x, gemPos.y - sizeGem)
            lineTo(gemPos.x + sizeGem, gemPos.y)
            lineTo(gemPos.x, gemPos.y + sizeGem)
            lineTo(gemPos.x - sizeGem, gemPos.y)
            close()
        }

        drawPath(diamondPath, color = Color.White, style = Fill)
        drawPath(diamondPath, color = colors[i % colors.size], style = Stroke(width = 1.5f))
    }
}

// Disco strobe rhythm dividing border into flashing neon segments
private fun DrawScope.drawDiscoStrobeBorder(
    w: Float,
    h: Float,
    r: Float,
    progress: Float,
    pulse: Float,
    colors: List<Color>,
    strokeWidth: Float
) {
    val inset = strokeWidth / 2f
    val segmentCount = 20

    for (i in 0 until segmentCount) {
        val segStart = (i.toFloat() / segmentCount + progress) % 1f
        val segEnd = ((i + 0.7f) / segmentCount + progress) % 1f

        val ptStart = getPerimeterPoint(segStart, w - strokeWidth, h - strokeWidth, r)
        val ptEnd = getPerimeterPoint(segEnd, w - strokeWidth, h - strokeWidth, r)

        val isStrobe = ((i + (progress * 20).toInt()) % 2 == 0)
        val col = if (isStrobe) colors[i % colors.size] else colors[(i + 2) % colors.size]
        val alpha = if (isStrobe) 0.95f else (0.2f + 0.4f * pulse)

        drawLine(
            color = col,
            start = Offset(ptStart.x + inset, ptStart.y + inset),
            end = Offset(ptEnd.x + inset, ptEnd.y + inset),
            strokeWidth = strokeWidth * (if (isStrobe) 1.3f else 0.8f),
            cap = StrokeCap.Round,
            alpha = alpha
        )
    }
}

// Compute (x, y) along the perimeter of a rounded rectangle of size (w, h) with corner radius r
fun getPerimeterPoint(t: Float, w: Float, h: Float, r: Float): Offset {
    val normT = ((t % 1f) + 1f) % 1f

    val topLen = (w - 2 * r).coerceAtLeast(0f)
    val rightLen = (h - 2 * r).coerceAtLeast(0f)
    val bottomLen = topLen
    val leftLen = rightLen
    val arcLen = (PI.toFloat() * r / 2f).coerceAtLeast(0.001f)

    val totalPerimeter = topLen + rightLen + bottomLen + leftLen + (4 * arcLen)
    val targetDist = normT * totalPerimeter

    var currentDist = 0f

    // 1. Top edge: from (r, 0) to (w - r, 0)
    if (targetDist <= currentDist + topLen) {
        val fraction = (targetDist - currentDist) / topLen.coerceAtLeast(1f)
        return Offset(r + fraction * topLen, 0f)
    }
    currentDist += topLen

    // 2. Top-Right Arc: center (w - r, r), angles -PI/2 to 0
    if (targetDist <= currentDist + arcLen) {
        val fraction = (targetDist - currentDist) / arcLen
        val angle = -PI.toFloat() / 2f + (fraction * PI.toFloat() / 2f)
        return Offset((w - r) + r * cos(angle), r + r * sin(angle))
    }
    currentDist += arcLen

    // 3. Right edge: from (w, r) to (w, h - r)
    if (targetDist <= currentDist + rightLen) {
        val fraction = (targetDist - currentDist) / rightLen.coerceAtLeast(1f)
        return Offset(w, r + fraction * rightLen)
    }
    currentDist += rightLen

    // 4. Bottom-Right Arc: center (w - r, h - r), angles 0 to PI/2
    if (targetDist <= currentDist + arcLen) {
        val fraction = (targetDist - currentDist) / arcLen
        val angle = 0f + (fraction * PI.toFloat() / 2f)
        return Offset((w - r) + r * cos(angle), (h - r) + r * sin(angle))
    }
    currentDist += arcLen

    // 5. Bottom edge: from (w - r, h) to (r, h)
    if (targetDist <= currentDist + bottomLen) {
        val fraction = (targetDist - currentDist) / bottomLen.coerceAtLeast(1f)
        return Offset((w - r) - fraction * bottomLen, h)
    }
    currentDist += bottomLen

    // 6. Bottom-Left Arc: center (r, h - r), angles PI/2 to PI
    if (targetDist <= currentDist + arcLen) {
        val fraction = (targetDist - currentDist) / arcLen
        val angle = PI.toFloat() / 2f + (fraction * PI.toFloat() / 2f)
        return Offset(r + r * cos(angle), (h - r) + r * sin(angle))
    }
    currentDist += arcLen

    // 7. Left edge: from (0, h - r) to (0, r)
    if (targetDist <= currentDist + leftLen) {
        val fraction = (targetDist - currentDist) / leftLen.coerceAtLeast(1f)
        return Offset(0f, (h - r) - fraction * leftLen)
    }
    currentDist += leftLen

    // 8. Top-Left Arc: center (r, r), angles PI to 3*PI/2
    val fraction = ((targetDist - currentDist) / arcLen).coerceIn(0f, 1f)
    val angle = PI.toFloat() + (fraction * PI.toFloat() / 2f)
    return Offset(r + r * cos(angle), r + r * sin(angle))
}

private fun resolveBorderColors(palette: LightPalette): List<Color> {
    return when (palette) {
        LightPalette.SPIRITUAL_GOLD -> listOf(
            GoldAccent,
            GoldLight,
            EmeraldPrimary,
            TurquoiseNeon,
            Color(0xFFFFF0A0),
            GoldAccent
        )
        LightPalette.RAINBOW_NEON -> listOf(
            Color(0xFFFF0055),
            Color(0xFFFF7700),
            Color(0xFFFFEE00),
            Color(0xFF00FF66),
            Color(0xFF00E5FF),
            Color(0xFF9D00FF),
            Color(0xFFFF0055)
        )
        LightPalette.CYAN_AURORA -> listOf(
            Color(0xFF00E5FF),
            Color(0xFF1DE9B6),
            Color(0xFF00B0FF),
            Color(0xFF76FF03),
            Color(0xFF00E5FF)
        )
        LightPalette.COSMIC_VIOLET -> listOf(
            Color(0xFFD500F9),
            Color(0xFF651FFF),
            Color(0xFFFF4081),
            Color(0xFF00E5FF),
            Color(0xFFD500F9)
        )
        LightPalette.WARM_AMBER -> listOf(
            Color(0xFFFF3D00),
            Color(0xFFFF9100),
            Color(0xFFFFD600),
            Color(0xFFFF6D00),
            Color(0xFFFF3D00)
        )
        LightPalette.DIAMOND_WHITE -> listOf(
            Color(0xFFFFFFFF),
            Color(0xFF80D8FF),
            Color(0xFFE0F7FA),
            Color(0xFFB388FF),
            Color(0xFFFFFFFF)
        )
    }
}
