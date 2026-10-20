package com.example.ui.screens.body

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryCard
import com.example.ui.theme.SecondaryText

@Composable
fun BodyFigureOverview(
    state: BodyMeasurementsUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SecondaryCard)
            .padding(16.dp)
    ) {
        // Body silhouette with callout labels on left and right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Callouts Column (Neck, Chest, Left Arm, Waist, Left Thigh, Left Calf)
            Column(
                modifier = Modifier.weight(1.1f),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                BodyPartCallout(
                    label = "Neck",
                    delta = state.neckDelta,
                    alignEnd = false
                )
                Spacer(modifier = Modifier.height(18.dp))
                BodyPartCallout(
                    label = "Chest",
                    delta = state.chestDelta,
                    alignEnd = false
                )
                Spacer(modifier = Modifier.height(20.dp))
                BodyPartCallout(
                    label = "Left Arm",
                    delta = state.leftArmDelta,
                    alignEnd = false
                )
                Spacer(modifier = Modifier.height(24.dp))
                BodyPartCallout(
                    label = "Waist",
                    delta = state.waistDelta,
                    alignEnd = false
                )
                Spacer(modifier = Modifier.height(30.dp))
                BodyPartCallout(
                    label = "Left Thigh",
                    delta = state.leftThighDelta,
                    alignEnd = false
                )
                Spacer(modifier = Modifier.height(30.dp))
                BodyPartCallout(
                    label = "Left Calf",
                    delta = state.leftCalfDelta,
                    alignEnd = false
                )
            }

            // Center: Athletic Male Silhouette image matching reference
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(390.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_body_silhouette),
                    contentDescription = "Body Silhouette",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Right Callouts Column (Shoulders, Right Arm, Hips, Right Thigh, Right Calf)
            Column(
                modifier = Modifier.weight(1.1f),
                horizontalAlignment = Alignment.End
            ) {
                Spacer(modifier = Modifier.height(36.dp))
                BodyPartCallout(
                    label = "Shoulders",
                    delta = state.shouldersDelta,
                    alignEnd = true
                )
                Spacer(modifier = Modifier.height(32.dp))
                BodyPartCallout(
                    label = "Right Arm",
                    delta = state.rightArmDelta,
                    alignEnd = true
                )
                Spacer(modifier = Modifier.height(36.dp))
                BodyPartCallout(
                    label = "Hips",
                    delta = state.hipsDelta,
                    alignEnd = true
                )
                Spacer(modifier = Modifier.height(32.dp))
                BodyPartCallout(
                    label = "Right Thigh",
                    delta = state.rightThighDelta,
                    alignEnd = true
                )
                Spacer(modifier = Modifier.height(32.dp))
                BodyPartCallout(
                    label = "Right Calf",
                    delta = state.rightCalfDelta,
                    alignEnd = true
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tip Card at bottom of body figure
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF141522))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Compare with previous records to see your progress over time.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SecondaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
fun BodyPartCallout(
    label: String,
    delta: MeasurementSummaryDelta,
    alignEnd: Boolean
) {
    val alignment = if (alignEnd) Alignment.End else Alignment.Start

    Column(horizontalAlignment = alignment) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = SecondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = if (delta.currentVal != null) "${String.format(java.util.Locale.getDefault(), "%.1f", delta.currentVal)} cm" else "--",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = PrimaryText,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        )
        if (delta.deltaSimple != null) {
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = delta.deltaSimple,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = NeonEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}
