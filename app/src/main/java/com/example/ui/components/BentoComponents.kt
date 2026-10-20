package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBentoBorder
import com.example.ui.theme.CardBentoDark
import com.example.ui.theme.MutedText
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import java.text.DateFormatSymbols
import java.util.Calendar

@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = CardBentoDark,
    borderColor: Color = CardBentoBorder,
    cornerRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick)
    } else {
        modifier
    }

    Box(
        modifier = cardModifier
            .background(backgroundColor, RoundedCornerShape(cornerRadius))
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .padding(18.dp)
    ) {
        content()

        if (onSettingsClick != null) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Options",
                    tint = MutedText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ProgressRingIndicator(
    number: Int,
    progress: Float = 0.45f,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    ringColor: Color = Color.White,
    backgroundColor: Color = Color(0xFF282830)
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 3.dp.toPx()
            val arcSize = size.toPx() - strokeWidth

            // Background circle
            drawArc(
                color = backgroundColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth)
            )

            // Progress arc
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = (progress * 360f).coerceIn(10f, 360f),
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = "$number",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                fontSize = 16.sp
            )
        )
    }
}

@Composable
fun ActivityDotHeatmap(
    completedWorkoutDays: Set<Long>,
    activeColor: Color = com.example.ui.theme.NeonEmerald,
    modifier: Modifier = Modifier
) {
    val monthsData = androidx.compose.runtime.remember(completedWorkoutDays) {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val monthNames = DateFormatSymbols.getInstance().shortMonths

        (2 downTo 0).map { offset ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth)
                add(Calendar.MONTH, -offset)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val mIndex = cal.get(Calendar.MONTH)
            val mName = monthNames.getOrNull(mIndex) ?: "Mon"
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            val daysActive = (1..daysInMonth).map { day ->
                val dayCal = Calendar.getInstance().apply {
                    timeInMillis = cal.timeInMillis
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val dayStart = dayCal.timeInMillis
                val dayEnd = dayStart + (24L * 3600 * 1000)

                completedWorkoutDays.any { it in dayStart until dayEnd }
            }
            Triple(mName, daysInMonth, daysActive)
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        monthsData.forEach { (mName, daysInMonth, daysActive) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF151620))
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in 0..3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            for (col in 0..6) {
                                val dayIndex = row * 7 + col
                                val isActive = if (dayIndex < daysActive.size) daysActive[dayIndex] else false

                                Box(
                                    modifier = Modifier
                                        .size(6.5.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isActive) activeColor else Color(0xFF26262E)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
