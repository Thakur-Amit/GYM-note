package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.DividerDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PurpleGradientEnd
import com.example.ui.theme.PurpleGradientStart
import com.example.ui.theme.SecondaryCard
import com.example.ui.theme.SecondaryIndigo
import com.example.ui.theme.SecondaryText
import kotlin.math.roundToInt

data class ChartBarData(
    val label: String,
    val value: Double,
    val detail: String = ""
)

data class ChartLinePoint(
    val label: String,
    val value: Double,
    val timestamp: Long
)

@Composable
fun VolumeBarChart(
    data: List<ChartBarData>,
    unit: String = "kg",
    modifier: Modifier = Modifier
) {
    if (data.isEmpty() || data.all { it.value <= 0.0 }) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SecondaryCard),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No volume data recorded in this period",
                style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
            )
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxValue = (data.maxOfOrNull { it.value } ?: 1.0).coerceAtLeast(10.0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SecondaryCard)
            .padding(16.dp)
    ) {
        // Selected highlight info banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRAINING VOLUME ($unit)".uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MutedText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            val selected = selectedIndex?.let { data.getOrNull(it) } ?: data.lastOrNull()
            if (selected != null) {
                Text(
                    text = "${selected.label}: ${selected.value.roundToInt()} $unit",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            val slotWidth = size.width / data.size
                            val index = (offset.x / slotWidth).toInt().coerceIn(0, data.size - 1)
                            selectedIndex = index
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height - 24.dp.toPx() // leave room for labels
                val barCount = data.size
                val barSpacing = 8.dp.toPx()
                val totalSpacing = barSpacing * (barCount + 1)
                val barWidth = ((canvasWidth - totalSpacing) / barCount).coerceIn(12.dp.toPx(), 44.dp.toPx())

                // Draw background grid lines
                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = canvasHeight * (i.toFloat() / gridLines)
                    drawLine(
                        color = DividerDark,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw bars
                data.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    val barHeight = ((item.value / maxValue) * (canvasHeight - 10f)).toFloat().coerceAtLeast(4f)
                    val x = barSpacing + index * (barWidth + barSpacing)
                    val y = canvasHeight - barHeight

                    val brush = if (isSelected) {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFC084FC), PurpleGradientStart)
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(PurpleGradientStart, PurpleGradientEnd)
                        )
                    }

                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEachIndexed { index, item ->
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (selectedIndex == index) PrimaryText else MutedText,
                        fontSize = 10.sp,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }
        }
    }
}

@Composable
fun WeightLineChart(
    points: List<ChartLinePoint>,
    unit: String = "kg",
    modifier: Modifier = Modifier
) {
    if (points.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SecondaryCard),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Record at least 2 body weight measurements to view trend",
                style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
            )
        }
        return
    }

    val minWeight = points.minOf { it.value } - 1.0
    val maxWeight = points.maxOf { it.value } + 1.0
    val range = (maxWeight - minWeight).coerceAtLeast(1.0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SecondaryCard)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WEIGHT TREND ($unit)".uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MutedText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            val latest = points.last()
            Text(
                text = "Latest: ${String.format("%.1f", latest.value)} $unit",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = PrimaryPurple,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height - 24.dp.toPx()

                // Draw horizontal grid lines
                val gridCount = 3
                for (i in 0..gridCount) {
                    val y = canvasHeight * (i.toFloat() / gridCount)
                    drawLine(
                        color = DividerDark,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val coords = points.mapIndexed { index, pt ->
                    val x = (index.toFloat() / (points.size - 1).coerceAtLeast(1)) * canvasWidth
                    val y = canvasHeight - (((pt.value - minWeight) / range) * canvasHeight).toFloat()
                    Offset(x, y)
                }

                // Fill area gradient
                val fillPath = Path().apply {
                    moveTo(coords.first().x, canvasHeight)
                    coords.forEach { lineTo(it.x, it.y) }
                    lineTo(coords.last().x, canvasHeight)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PrimaryPurple.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )

                // Line path
                val strokePath = Path().apply {
                    coords.forEachIndexed { idx, pt ->
                        if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                    }
                }

                drawPath(
                    path = strokePath,
                    color = PrimaryPurple,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Dots on points
                coords.forEach { pt ->
                    drawCircle(
                        color = BackgroundDark,
                        radius = 5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = PrimaryPurple,
                        radius = 3.5.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        // X-axis date labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = points.first().label,
                style = MaterialTheme.typography.labelSmall.copy(color = MutedText, fontSize = 10.sp)
            )
            if (points.size > 2) {
                Text(
                    text = points[points.size / 2].label,
                    style = MaterialTheme.typography.labelSmall.copy(color = MutedText, fontSize = 10.sp)
                )
            }
            Text(
                text = points.last().label,
                style = MaterialTheme.typography.labelSmall.copy(color = MutedText, fontSize = 10.sp)
            )
        }
    }
}

@Composable
fun MuscleDistributionChart(
    distributionMap: Map<String, Double>,
    unit: String = "kg",
    modifier: Modifier = Modifier
) {
    val totalVolume = distributionMap.values.sum().coerceAtLeast(1.0)
    val sortedList = distributionMap.entries
        .sortedByDescending { it.value }
        .filter { it.value > 0.0 }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SecondaryCard)
            .padding(16.dp)
    ) {
        Text(
            text = "VOLUME BY MUSCLE GROUP".uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MutedText,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (sortedList.isEmpty()) {
            Text(
                text = "No completed sets recorded in this period",
                style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
            )
        } else {
            sortedList.take(6).forEach { entry ->
                val fraction = (entry.value / totalVolume).toFloat()
                val percent = (fraction * 100).roundToInt()

                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = entry.key,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = PrimaryText,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "${entry.value.roundToInt()} $unit ($percent%)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SecondaryText
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElevatedSurface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(PurpleGradientStart, SecondaryIndigo)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
