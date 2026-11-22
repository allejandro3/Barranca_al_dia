package com.example.reportesciudadano.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reportesciudadano.ui.theme.ChartColors
import com.example.reportesciudadano.ui.theme.DarkGreen
import com.example.reportesciudadano.ui.theme.LimeGreen
import com.example.reportesciudadano.ui.theme.StatusColors
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun DonutChart(
    values: List<Float>,
    colors: List<Color>,
    labels: List<String> = emptyList(),
    onSliceClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val total = values.sum().coerceAtLeast(1f)
    var activeIndex by remember { mutableStateOf<Int?>(null) }
    var tooltipText by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(values) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val distance = hypot(dx, dy)
                        val diameter = minOf(size.width, size.height).toFloat()
                        val strokeWidth = diameter * 0.25f
                        val outerRadius = diameter / 2f
                        val innerRadius = outerRadius - strokeWidth

                        if (distance in innerRadius..outerRadius) {
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            var adjustedAngle = angle + 90f
                            if (adjustedAngle >= 360f) adjustedAngle -= 360f

                            var currentAngle = 0f
                            values.forEachIndexed { i, value ->
                                val sweepAngle = (value / total) * 360f
                                if (adjustedAngle in currentAngle..(currentAngle + sweepAngle)) {
                                    if (activeIndex == i) {
                                        activeIndex = null
                                        tooltipText = null
                                        onSliceClick(-1)
                                    } else {
                                        activeIndex = i
                                        val label = labels.getOrElse(i) { "Ítem $i" }
                                        tooltipText = "$label: ${values[i].toInt()}"
                                        onSliceClick(i)
                                    }
                                }
                                currentAngle += sweepAngle
                            }
                        }
                    }
                }
        ) {
            val diameter = size.minDimension
            val strokeWidth = diameter * 0.25f
            val arcSize = Size(diameter - strokeWidth, diameter - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            var startAngle = -90f
            values.forEachIndexed { i, value ->
                val sweepAngle = (value / total) * 360f
                val isSelected = activeIndex == i
                drawArc(
                    color = colors.getOrElse(i) { Color.Gray },
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = if (isSelected) strokeWidth * 1.25f else strokeWidth)
                )
                startAngle += sweepAngle
            }
        }

        // Tooltip Popup Box with Animation
        AnimatedVisibility(
            visible = tooltipText != null,
            enter = fadeIn() + slideInVerticallyAnimation(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            tooltipText?.let { text ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.9f))
                        .border(1.dp, LimeGreen, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PolarAreaChart(
    values: List<Float>,
    labels: List<String>,
    colors: List<Color>,
    onSectorClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val maxVal = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    var activeIndex by remember { mutableStateOf<Int?>(null) }
    var tooltipText by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(values) {
                    detectTapGestures { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val distance = hypot(dx, dy)
                        val maxRadius = minOf(size.width, size.height) / 2f * 0.85f

                        if (distance <= maxRadius) {
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            var adjustedAngle = angle + 90f
                            if (adjustedAngle >= 360f) adjustedAngle -= 360f

                            val sliceAngle = 360f / values.size
                            val clickedIdx = (adjustedAngle / sliceAngle).toInt().coerceIn(0, values.size - 1)

                            if (activeIndex == clickedIdx) {
                                activeIndex = null
                                tooltipText = null
                                onSectorClick(-1)
                            } else {
                                activeIndex = clickedIdx
                                val label = labels.getOrElse(clickedIdx) { "" }
                                tooltipText = "$label: ${values[clickedIdx].toInt()}"
                                onSectorClick(clickedIdx)
                            }
                        }
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = minOf(size.width, size.height) / 2f * 0.85f
            val sliceAngle = 360f / values.size

            // Draw concentric background circles
            for (r in 1..3) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = maxRadius * (r / 3f),
                    center = center,
                    style = Stroke(width = 1.5f)
                )
            }

            // Draw wedges
            values.forEachIndexed { i, value ->
                val radius = maxRadius * (value / maxVal).coerceIn(0.2f, 1.0f)
                val startAngle = -90f + i * sliceAngle
                val isSelected = activeIndex == i

                drawArc(
                    color = colors.getOrElse(i) { Color.Gray }.copy(alpha = if (isSelected) 1.0f else 0.85f),
                    startAngle = startAngle,
                    sweepAngle = sliceAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f)
                )
                drawArc(
                    color = DarkGreen,
                    startAngle = startAngle,
                    sweepAngle = sliceAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = 2f)
                )
            }
        }

        AnimatedVisibility(visible = tooltipText != null, modifier = Modifier.align(Alignment.TopCenter)) {
            tooltipText?.let { text ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.9f))
                        .border(1.dp, LimeGreen, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HorizontalBarChart(
    labels: List<String>,
    values: List<Int>,
    barColor: Color = LimeGreen,
    onBarClick: (Int) -> Unit = {}
) {
    val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    var activeIndex by remember { mutableStateOf<Int?>(null) }
    var tooltipText by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AnimatedVisibility(visible = tooltipText != null, enter = fadeIn(), exit = fadeOut()) {
            tooltipText?.let { text ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.9f))
                            .border(1.dp, LimeGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        labels.forEachIndexed { index, label ->
            val value = values.getOrElse(index) { 0 }
            val fraction = (value.toFloat() / maxValue).coerceIn(0.05f, 1.0f)
            val isSelected = activeIndex == index

            val animatedFraction by animateFloatAsState(
                targetValue = fraction,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                label = "bar_width"
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) Color.White.copy(0.2f) else Color.Transparent)
                    .clickable {
                        if (activeIndex == index) {
                            activeIndex = null
                            tooltipText = null
                            onBarClick(-1)
                        } else {
                            activeIndex = index
                            tooltipText = "$label: $value reportes"
                            onBarClick(index)
                        }
                    }
                    .padding(4.dp)
            ) {
                Text(
                    text = label,
                    color = if (isSelected) LimeGreen else Color.White,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.width(80.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedFraction)
                            .fillMaxHeight()
                            .background(if (isSelected) Color.White else barColor, RoundedCornerShape(4.dp))
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = value.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(30.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun AreaChart(
    values: List<Int>,
    onPointClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val maxVal = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    var activeIdx by remember { mutableStateOf<Int?>(null) }
    var tooltipText by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(values) {
                    detectTapGestures { offset ->
                        val w = size.width.toFloat()
                        val stepX = w / (values.size - 1).coerceAtLeast(1)
                        val clickedIndex = (offset.x / stepX).roundToInt().coerceIn(0, values.size - 1)
                        if (activeIdx == clickedIndex) {
                            activeIdx = null
                            tooltipText = null
                            onPointClick(-1)
                        } else {
                            activeIdx = clickedIndex
                            tooltipText = "Hora ${clickedIndex}h: ${values[clickedIndex]} reportes"
                            onPointClick(clickedIndex)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val stepX = w / (values.size - 1).coerceAtLeast(1)

            val path = Path().apply {
                moveTo(0f, h - (values[0].toFloat() / maxVal) * h)
                for (i in 1 until values.size) {
                    val x = i * stepX
                    val y = h - (values[i].toFloat() / maxVal) * h
                    lineTo(x, y)
                }
            }

            drawPath(path, color = LimeGreen, style = Stroke(width = 4f))

            val fillPath = Path().apply {
                addPath(path)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(fillPath, color = LimeGreen.copy(alpha = 0.2f))

            activeIdx?.let { idx ->
                val cx = idx * stepX
                val cy = h - (values[idx].toFloat() / maxVal) * h
                drawCircle(Color.White, radius = 8f, center = Offset(cx, cy))
                drawCircle(LimeGreen, radius = 5f, center = Offset(cx, cy))
            }
        }

        AnimatedVisibility(visible = tooltipText != null, modifier = Modifier.align(Alignment.TopCenter)) {
            tooltipText?.let { text ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.9f))
                        .border(1.dp, LimeGreen, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyHeatmap(
    matrix: List<List<Int>>,
    days: List<String>,
    bands: List<String>,
    onCellClick: (Int, Int) -> Unit
) {
    val maxVal = matrix.flatMap { it }.maxOrNull()?.coerceAtLeast(1) ?: 1
    var activeCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var tooltipText by remember { mutableStateOf<String?>(null) }

    Column {
        AnimatedVisibility(visible = tooltipText != null) {
            tooltipText?.let { text ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.9f))
                            .border(1.dp, LimeGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Spacer(modifier = Modifier.width(32.dp))
            bands.forEach { b ->
                Text(
                    text = b.replace(" h", "").replace("–", "-"),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        days.forEachIndexed { dayIdx, day ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day.take(3),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(32.dp)
                )

                matrix.getOrElse(dayIdx) { emptyList() }.forEachIndexed { bandIdx, count ->
                    val alpha = 0.15f + 0.85f * (count.toFloat() / maxVal)
                    val cellColor = LimeGreen.copy(alpha = alpha.coerceIn(0.15f, 1.0f))
                    val isSelected = activeCell == Pair(dayIdx, bandIdx)
                    val textColor = if (alpha > 0.55f || isSelected) DarkGreen else Color.White

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .padding(1.5.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color.White else cellColor)
                            .clickable {
                                if (activeCell == Pair(dayIdx, bandIdx)) {
                                    activeCell = null
                                    tooltipText = null
                                    onCellClick(-1, -1)
                                } else {
                                    activeCell = Pair(dayIdx, bandIdx)
                                    tooltipText = "$day (${bands.getOrElse(bandIdx) { "" }}): $count reportes"
                                    onCellClick(dayIdx, bandIdx)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = count.toString(),
                            color = textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VerticalBarChart(
    labels: List<String>,
    values: List<Int>,
    onBarClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    var activeIndex by remember { mutableStateOf<Int?>(null) }
    var tooltipText by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier) {
        AnimatedVisibility(visible = tooltipText != null) {
            tooltipText?.let { text ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.9f))
                            .border(1.dp, LimeGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            labels.forEachIndexed { i, label ->
                val value = values.getOrElse(i) { 0 }
                val fraction = (value.toFloat() / maxValue).coerceIn(0.05f, 1.0f)
                val isSelected = activeIndex == i

                val animatedFraction by animateFloatAsState(
                    targetValue = fraction,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                    label = "vbar_height"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (activeIndex == i) {
                                activeIndex = null
                                tooltipText = null
                                onBarClick(-1)
                            } else {
                                activeIndex = i
                                tooltipText = "$label: $value reportes"
                                onBarClick(i)
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(100.dp)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(animatedFraction)
                                .background(if (isSelected) Color.White else LimeGreen, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        color = if (isSelected) LimeGreen else Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private fun slideInVerticallyAnimation() = slideInVertically(initialOffsetY = { -it })
