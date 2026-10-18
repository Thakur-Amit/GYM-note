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
import com.example.ui.theme.AccentWarning
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBentoBorder
import com.example.ui.theme.CardBentoDark
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
    onNavigateToProfile: () -> Unit
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
        // 1. Top Bar: Title "Workouts" with Tune & Plus circular buttons (Image 2)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Workouts",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText,
                        fontSize = 36.sp
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Filter / Options button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CardBentoDark)
                            .border(1.dp, CardBentoBorder, CircleShape)
                            .clickable(onClick = onNavigateToProgress),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Options",
                            tint = PrimaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Add Workout Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple)
                            .clickable {
                                viewModel.startAdHocWorkout(onNavigateToActiveWorkout)
                            }
                            .testTag("home_add_workout_top_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Start Workout",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
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

        // 2. Top Hero Card: High-Impact Workout Banner Artwork matching reference UI
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(185.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, PrimaryPurple.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                    .clickable {
                        viewModel.startRoutine(state.routine1.templateId, onNavigateToActiveWorkout)
                    }
                    .testTag("home_hero_card")
            ) {
                // Background athletic photo art
                Image(
                    painter = painterResource(id = R.drawable.img_home_hero),
                    contentDescription = "Workout Hero",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // High contrast dark gradient scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x9909090B),
                                    Color(0xF509090B)
                                )
                            )
                        )
                )

                // Overlay Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryPurple.copy(alpha = 0.9f))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Text(
                            text = "FEATURED SPLIT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    // Bottom info & launch button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.routine1.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryText,
                                    fontSize = 22.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.routine1.scheduleSubtitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = SecondaryText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1
                            )
                        }

                        // Circular play button
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start Workout",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
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
                        viewModel.startRoutine(state.routine1.templateId, onNavigateToActiveWorkout)
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
                                text = state.routine1.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryText,
                                    fontSize = 19.sp
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
                val unit = state.userProfile.preferredWeightUnit

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
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonCyan,
                                    fontSize = 38.sp
                                )
                            )
                            if (state.currentWeight != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = unit,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = SecondaryText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
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
                            Text(
                                text = state.weightSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (state.currentWeight == null) NeonCyan else SecondaryText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. Center Bento Card (Activity Dot Heatmap + Routine 2)
        item {
            BentoCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                onSettingsClick = onNavigateToReports
            ) {
                Column {
                    // Header label above dot heatmap
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

                    // Activity Heatmap Dot Matrix
                    ActivityDotHeatmap(
                        completedWorkoutDays = state.completedWorkoutDays,
                        activeColor = NeonEmerald,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    // Routine 2 Item (e.g. Pull Day / Real second routine)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.startRoutine(state.routine2.templateId, onNavigateToActiveWorkout)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProgressRingIndicator(
                            number = state.routine2.number,
                            progress = state.routine2.progress,
                            size = 56.dp,
                            ringColor = NeonEmerald
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.routine2.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryText,
                                    fontSize = 19.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.routine2.scheduleSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SecondaryText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                maxLines = 1
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
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

        // 6. Full-width Quick Add Tile with Bold Text
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardBentoDark)
                    .border(1.dp, PrimaryPurple.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                    .clickable {
                        viewModel.startAdHocWorkout(onNavigateToActiveWorkout)
                    }
                    .testTag("home_quick_add_tile"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Workout",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Start Empty Workout",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            fontSize = 17.sp
                        )
                    )
                }
            }
        }
    }

    if (showQuickWeightDialog) {
        AddWeightDialog(
            unit = state.userProfile.preferredWeightUnit,
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
