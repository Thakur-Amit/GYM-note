package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DividerDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

data class ExerciseSessionPoint(
    val dateMillis: Long,
    val maxWeight: Double,
    val estimated1RM: Double,
    val bestReps: Int
)

@Composable
fun ExerciseProgressChart(
    points: List<ExerciseSessionPoint>,
    unit: String = "kg",
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PrimaryCard)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = MutedText,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No Progress History Yet",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Log and finish completed sets to track 1RM and Max Weight curves.",
                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    var show1RM by remember { mutableStateOf(true) }
    var showMaxWeight by remember { mutableStateOf(true) }
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    val sortedPoints = remember(points) { points.sortedBy { it.dateMillis } }
    val maxVal = remember(sortedPoints, show1RM, showMaxWeight) {
        val peak1RM = if (show1RM) sortedPoints.maxOfOrNull { it.estimated1RM } ?: 0.0 else 0.0
        val peakMaxWt = if (showMaxWeight) sortedPoints.maxOfOrNull { it.maxWeight } ?: 0.0 else 0.0
        max(peak1RM, peakMaxWt).coerceAtLeast(20.0)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PrimaryCard)
            .padding(16.dp)
    ) {
        // Top Header + Interactive Legend Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Strength Progression",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Toggle Estimated 1RM
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (show1RM) ElevatedSurface else Color.Transparent)
                        .clickable { show1RM = !show1RM }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (show1RM) NeonCyan else MutedText)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Est. 1RM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (show1RM) PrimaryText else MutedText,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Toggle Max Weight
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (showMaxWeight) ElevatedSurface else Color.Transparent)
                        .clickable { showMaxWeight = !showMaxWeight }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (showMaxWeight) PrimaryPurple else MutedText)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Max Wt",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (showMaxWeight) PrimaryText else MutedText,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Point Inspector Tooltip
        selectedPointIndex?.let { idx ->
            if (idx in sortedPoints.indices) {
                val pt = sortedPoints[idx]
                val dateStr = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(pt.dateMillis))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElevatedSurface)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "1RM: ${pt.estimated1RM.toInt()} $unit",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                        Text(
                            text = "Max: ${pt.maxWeight.toInt()} $unit × ${pt.bestReps}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Canvas Dual-Series Line Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(sortedPoints) {
                        detectTapGestures { offset ->
                            val n = sortedPoints.size
                            if (n > 1) {
                                val paddingStart = 32.dp.toPx()
                                val paddingEnd = 16.dp.toPx()
                                val availW = size.width - paddingStart - paddingEnd
                                val stepX = availW / (n - 1)
                                val tappedIndex = ((offset.x - paddingStart + (stepX / 2)) / stepX).toInt()
                                if (tappedIndex in 0 until n) {
                                    selectedPointIndex = tappedIndex
                                }
                            } else if (n == 1) {
                                selectedPointIndex = 0
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                val paddingStart = 32.dp.toPx()
                val paddingEnd = 16.dp.toPx()
                val paddingTop = 16.dp.toPx()
                val paddingBottom = 24.dp.toPx()

                val chartW = w - paddingStart - paddingEnd
                val chartH = h - paddingTop - paddingBottom

                // Horizontal Reference Gridlines
                val gridSteps = 3
                for (i in 0..gridSteps) {
                    val y = paddingTop + (chartH * (i / gridSteps.toFloat()))
                    drawLine(
                        color = DividerDark.copy(alpha = 0.5f),
                        start = Offset(paddingStart, y),
                        end = Offset(w - paddingEnd, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                }

                if (sortedPoints.size == 1) {
                    // Single point center rendering
                    val pt = sortedPoints[0]
                    val cx = paddingStart + chartW / 2
                    val cy1RM = paddingTop + chartH * (1f - (pt.estimated1RM / maxVal).toFloat().coerceIn(0.1f, 0.95f))
                    val cyMax = paddingTop + chartH * (1f - (pt.maxWeight / maxVal).toFloat().coerceIn(0.1f, 0.95f))

                    if (show1RM) {
                        drawCircle(color = NeonCyan, radius = 6.dp.toPx(), center = Offset(cx, cy1RM))
                    }
                    if (showMaxWeight) {
                        drawCircle(color = PrimaryPurple, radius = 5.dp.toPx(), center = Offset(cx, cyMax))
                    }
                    return@Canvas
                }

                val n = sortedPoints.size
                val stepX = chartW / (n - 1)

                // 1. Draw Max Weight Series (Purple dashed)
                if (showMaxWeight) {
                    val maxPath = Path()
                    sortedPoints.forEachIndexed { i, pt ->
                        val x = paddingStart + (i * stepX)
                        val y = paddingTop + chartH * (1f - (pt.maxWeight / maxVal).toFloat().coerceIn(0.05f, 0.95f))
                        if (i == 0) maxPath.moveTo(x, y) else maxPath.lineTo(x, y)
                    }
                    drawPath(
                        path = maxPath,
                        color = PrimaryPurple,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    )
                    sortedPoints.forEachIndexed { i, pt ->
                        val x = paddingStart + (i * stepX)
                        val y = paddingTop + chartH * (1f - (pt.maxWeight / maxVal).toFloat().coerceIn(0.05f, 0.95f))
                        drawCircle(color = PrimaryPurple, radius = 3.5.dp.toPx(), center = Offset(x, y))
                    }
                }

                // 2. Draw Estimated 1RM Series (Cyan solid)
                if (show1RM) {
                    val rmPath = Path()
                    sortedPoints.forEachIndexed { i, pt ->
                        val x = paddingStart + (i * stepX)
                        val y = paddingTop + chartH * (1f - (pt.estimated1RM / maxVal).toFloat().coerceIn(0.05f, 0.95f))
                        if (i == 0) rmPath.moveTo(x, y) else rmPath.lineTo(x, y)
                    }
                    drawPath(
                        path = rmPath,
                        color = NeonCyan,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    sortedPoints.forEachIndexed { i, pt ->
                        val x = paddingStart + (i * stepX)
                        val y = paddingTop + chartH * (1f - (pt.estimated1RM / maxVal).toFloat().coerceIn(0.05f, 0.95f))
                        drawCircle(color = Color(0xFF0F1016), radius = 5.5.dp.toPx(), center = Offset(x, y))
                        drawCircle(color = NeonCyan, radius = 4.dp.toPx(), center = Offset(x, y))

                        if (selectedPointIndex == i) {
                            drawCircle(color = NeonCyan.copy(alpha = 0.35f), radius = 10.dp.toPx(), center = Offset(x, y))
                        }
                    }
                }
            }
        }

        // Date axis labels at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, end = 12.dp, top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val firstDate = sortedPoints.firstOrNull()?.let {
                SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(it.dateMillis))
            } ?: ""
            val lastDate = sortedPoints.lastOrNull()?.let {
                SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(it.dateMillis))
            } ?: ""

            Text(firstDate, style = MaterialTheme.typography.labelSmall.copy(color = MutedText, fontSize = 11.sp))
            if (sortedPoints.size > 2) {
                val midDate = sortedPoints[sortedPoints.size / 2].let {
                    SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(it.dateMillis))
                }
                Text(midDate, style = MaterialTheme.typography.labelSmall.copy(color = MutedText, fontSize = 11.sp))
            }
            Text(lastDate, style = MaterialTheme.typography.labelSmall.copy(color = MutedText, fontSize = 11.sp))
        }
    }
}
