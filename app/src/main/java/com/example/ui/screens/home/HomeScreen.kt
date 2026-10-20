package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.ActivityDotHeatmap
import com.example.ui.components.BentoCard
import com.example.ui.components.GymCard
import com.example.ui.components.GymGradientButton
import com.example.ui.components.GymSecondaryButton
import com.example.ui.components.ProgressRingIndicator
import com.example.ui.screens.progress.AddWeightDialog
import com.example.ui.theme.AccentError
import com.example.ui.theme.AccentWarning
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBentoBorder
import com.example.ui.theme.CardBentoDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryCard
import com.example.ui.theme.SecondaryText
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToActiveWorkout: () -> Unit,
    onNavigateToExercises: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToBodyWeight: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToRoutineDetail: (String) -> Unit = {},
    onNavigateToBodyDetails: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showQuickWeightDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("home_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Top Bar: Clean Title "Workouts"
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "Workouts",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText,
                        fontSize = 36.sp
                    )
                )
            }
        }

        // Active Session in Progress recovery banner (if any)
        if (state.activeSession != null) {
            item {
                GymCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SecondaryCard,
                    borderColor = PrimaryPurple
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AccentWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Workout in Progress: ${state.activeSession?.session?.name}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText,
                                    fontSize = 17.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            GymGradientButton(
                                text = "Resume",
                                icon = Icons.Default.PlayArrow,
                                onClick = onNavigateToActiveWorkout,
                                modifier = Modifier.weight(1f)
                            )
                            GymSecondaryButton(
                                text = "Discard",
                                onClick = { viewModel.discardActiveWorkout() }
                            )
                        }
                    }
                }
            }
        }

        // In-app Body Measurement Reminder Card (Volume Lifted card shape, above all cards)
        if (state.bodyMeasurementReminder.showBanner) {
            item {
                BentoCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    onClick = onNavigateToBodyDetails,
                    onSettingsClick = onNavigateToBodyDetails
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val fillPct = state.bodyMeasurementReminder.completionPercentage.coerceIn(0f, 1f)
                        if (fillPct > 0f) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .fillMaxWidth(fillPct)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.20f))
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Update body details",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PrimaryText,
                                        fontSize = 18.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (fillPct > 0f) {
                                        "${(fillPct * 100).toInt()}% completed • ${state.bodyMeasurementReminder.subtitle}"
                                    } else {
                                        "Tap to enter body measurements"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (fillPct > 0f) NeonEmerald else SecondaryText,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElevatedSurface)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${(fillPct * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (fillPct > 0f) NeonEmerald else PrimaryPurple,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Row 1: Two Bigger Bento Cards (Routine 1 & Body Weight)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Card 1: Routine 1
                BentoCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(195.dp),
                    cornerRadius = 24.dp,
                    onClick = {
                        val tmplId = state.routine1.templateId
                        if (tmplId != null) {
                            onNavigateToRoutineDetail(tmplId)
                        } else {
                            onNavigateToExercises()
                        }
                    },
                    onSettingsClick = onNavigateToExercises
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProgressRingIndicator(
                            number = state.routine1.number,
                            progress = state.routine1.progress,
                            size = 56.dp,
                            ringColor = PrimaryPurple
                        )

                        Column {
                            Text(
                                text = state.routine1.dayLabel.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryPurple,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${state.routine1.dayLabel} • ${state.routine1.title}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryText,
                                    fontSize = 17.sp
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.routine1.scheduleSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SecondaryText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                maxLines = 2
                            )
                        }
                    }
                }

                // Card 2: Real Body Weight
                val unit = "kg"

                BentoCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(195.dp),
                    cornerRadius = 24.dp,
                    onClick = { showQuickWeightDialog = true },
                    onSettingsClick = onNavigateToBodyWeight
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (state.currentWeight != null) "${state.currentWeight?.roundToInt()}" else "--",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan,
                                    fontSize = 52.sp
                                )
                            )
                            if (state.currentWeight != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = unit,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = SecondaryText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Body weight",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryText,
                                    fontSize = 19.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.weightSubtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (state.currentWeight == null) NeonCyan else SecondaryText,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                )
                                if (state.weightDeltaStr != null) {
                                    val isPos = state.weightDeltaPositive == true
                                    Text(
                                        text = state.weightDeltaStr!!,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isPos) NeonEmerald else AccentError
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Center Bento Card (Activity Dot Heatmap)
        item {
            BentoCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                onSettingsClick = onNavigateToReports
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Activity Log",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryText,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "${state.completedWorkoutDays.size} workouts completed",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (state.completedWorkoutDays.isNotEmpty()) NeonEmerald else SecondaryText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Activity Heatmap Dot Matrix (with enlarged dots)
                    ActivityDotHeatmap(
                        completedWorkoutDays = state.completedWorkoutDays,
                        activeColor = NeonEmerald,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 5. Volume Lifted Card (Real 7-day volume from Room DB)
        val unit = state.userProfile.preferredWeightUnit
        val volumeFormatted = String.format("%,.0f", state.volumeLifted7Days)

        item {
            BentoCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                onClick = onNavigateToProgress,
                onSettingsClick = onNavigateToReports
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Volume lifted",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryText,
                                fontSize = 19.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.volumeLifted7Days > 0) "Last 7 days" else "No lifting logged this week",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SecondaryText,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(end = 28.dp)
                    ) {
                        Text(
                            text = volumeFormatted,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryPurple,
                                fontSize = 32.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = SecondaryText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
            }
        }
    }

    if (showQuickWeightDialog) {
        AddWeightDialog(
            unit = "kg",
            heightCm = state.userProfile.heightCm,
            age = state.userProfile.calculateAge(),
            onConfirm = { weight, bodyFat, muscleMass, notes ->
                viewModel.logBodyWeight(weight, bodyFat, muscleMass, notes)
                showQuickWeightDialog = false
            },
            onDismiss = { showQuickWeightDialog = false }
        )
    }
}
