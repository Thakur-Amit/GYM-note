package com.example.ui.screens.progress

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ChartBarData
import com.example.ui.components.ChartLinePoint
import com.example.ui.components.GymCard
import com.example.ui.components.GymGradientButton
import com.example.ui.components.GymSecondaryButton
import com.example.ui.components.WeightLineChart
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.AccentWarning
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.DividerDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryCard
import com.example.ui.theme.SecondaryText
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    onNavigateToBodyWeight: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = state.summary

    var selectedSubTab by remember { mutableStateOf(0) } // 0 = Overview, 1 = Muscle Groups, 2 = Body Stats
    var showAddWeightDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("progress_screen")
    ) {
        // Header matching Image 1 Screens 9, 10, 11
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-tabs matching Image 1: [Overview] [Muscle Groups] [Body Stats]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryCard)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val subTabs = listOf("Overview", "Muscle Groups", "Body Stats")
                subTabs.forEachIndexed { index, title ->
                    val isSelected = selectedSubTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) PrimaryPurple else Color.Transparent)
                            .clickable { selectedSubTab = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PrimaryText else SecondaryText
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Range Filters: [1M] [3M] [6M] [1Y] [All]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val ranges = listOf(TimeRange.ONE_MONTH, TimeRange.THREE_MONTHS, TimeRange.SIX_MONTHS, TimeRange.ONE_YEAR, TimeRange.ALL_TIME)
                ranges.forEach { range ->
                    val isSelected = state.selectedRange == range
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryPurple else SecondaryCard)
                            .clickable { viewModel.onTimeRangeSelected(range) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = range.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PrimaryText else SecondaryText
                            )
                        )
                    }
                }
            }
        }

        // Sub-Tab Content
        when (selectedSubTab) {
            0 -> {
                // TAB 1: OVERVIEW (Real Database Calculations)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 4 Stat Cards in 2x2 grid
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OverviewStatPill(
                                title = "Total Workouts",
                                value = "${summary?.completedWorkoutsCount ?: 0}",
                                delta = if ((summary?.completedWorkoutsCount ?: 0) > 0) "Active" else "0",
                                modifier = Modifier.weight(1f)
                            )
                            OverviewStatPill(
                                title = "Total Volume",
                                value = if (summary != null && summary.totalVolume > 0) "${summary.totalVolume.roundToInt()} ${state.preferredUnit}" else "0 ${state.preferredUnit}",
                                delta = if ((summary?.totalVolume ?: 0.0) > 0) "Logged" else "0",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        val avgWeight = state.avgWeightLifted
                        val avgReps = state.avgReps
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OverviewStatPill(
                                title = "Avg. Weight Lifted",
                                value = if (avgWeight != null && avgWeight > 0) "${avgWeight.roundToInt()} ${state.preferredUnit}" else "--",
                                delta = if (avgWeight != null) "Per rep" else "--",
                                modifier = Modifier.weight(1f)
                            )
                            OverviewStatPill(
                                title = "Avg. Reps",
                                value = if (avgReps != null && avgReps > 0) String.format("%.1f", avgReps) else "--",
                                delta = if (avgReps != null) "Per set" else "--",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Progress Chart with Total Volume dropdown
                    item {
                        GymCard(backgroundColor = PrimaryCard) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Training Volume Progression",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryText
                                        )
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SecondaryCard,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DividerDark)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(state.preferredUnit, style = MaterialTheme.typography.labelSmall.copy(color = PrimaryText))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                val hasVolume = state.volumeChartData.any { it.value > 0 }
                                if (hasVolume) {
                                    val volumePoints = state.volumeChartData.mapIndexed { idx, bar ->
                                        ChartLinePoint(
                                            label = bar.label,
                                            value = bar.value,
                                            timestamp = idx.toLong()
                                        )
                                    }
                                    WeightLineChart(
                                        points = volumePoints,
                                        unit = state.preferredUnit
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .background(SecondaryCard, RoundedCornerShape(12.dp))
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No training volume recorded in this time window.\nComplete workouts to graph your volume progression curve.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = SecondaryText,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                lineHeight = 18.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Link to Monthly Report
                    item {
                        GymSecondaryButton(
                            text = "View Monthly Report Table",
                            icon = Icons.Default.Assessment,
                            onClick = onNavigateToReports,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            1 -> {
                // TAB 2: MUSCLE GROUPS (Real Volume Distribution from Room DB)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (state.muscleDistributions.isEmpty()) {
                        item {
                            GymCard(backgroundColor = PrimaryCard) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No Muscle Volume Recorded",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryText
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Completed workout exercises will automatically break down training volume across Chest, Back, Legs, Shoulders, Arms, and Core.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = SecondaryText,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            lineHeight = 18.sp
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.muscleDistributions) { item ->
                            GymCard(backgroundColor = PrimaryCard) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ElevatedSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FitnessCenter,
                                            contentDescription = null,
                                            tint = item.color,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = item.muscleGroup,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryText
                                                )
                                            )
                                            Text(
                                                text = "${item.percentage}% (${item.volume.roundToInt()} ${state.preferredUnit})",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = item.color
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Horizontal proportional progress bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(ElevatedSurface)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth((item.percentage / 100f).coerceIn(0.04f, 1f))
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(item.color)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // TAB 3: BODY STATS / COMPOSITION (Real Body Fat, BMI, and Muscle Mass Tracking)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Body weight smooth line chart
                    item {
                        GymCard(backgroundColor = PrimaryCard) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Weight & Composition Curve",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                if (state.weightChartData.size >= 2) {
                                    WeightLineChart(
                                        points = state.weightChartData,
                                        unit = state.preferredUnit
                                    )
                                } else if (state.weightChartData.size == 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .background(SecondaryCard, RoundedCornerShape(12.dp))
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "1 measurement logged (${state.latestWeight} ${state.preferredUnit}).\nLog a second weigh-in to plot your trend curve.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = SecondaryText,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                lineHeight = 18.sp
                                            )
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .background(SecondaryCard, RoundedCornerShape(12.dp))
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No body weight logged yet.\nTap '+ Log Body Composition' below to record your first measurement.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = SecondaryText,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                lineHeight = 18.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Current Weight & Change Cards
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            GymCard(
                                backgroundColor = PrimaryCard,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Current Weight", style = MaterialTheme.typography.labelSmall.copy(color = MutedText))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (state.latestWeight != null) "${state.latestWeight} ${state.preferredUnit}" else "--",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryText
                                        )
                                    )
                                }
                            }

                            GymCard(
                                backgroundColor = PrimaryCard,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Weight Change", style = MaterialTheme.typography.labelSmall.copy(color = MutedText))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val delta = state.weightDelta
                                        Text(
                                            text = if (delta != null) "${if (delta > 0) "+" else ""}${String.format("%.1f", delta)} ${state.preferredUnit}" else "--",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryText
                                            )
                                        )
                                        if (delta != null) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = if (delta <= 0) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                                contentDescription = null,
                                                tint = if (delta <= 0) AccentSuccess else NeonAmber,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Other Stats: Body Fat %, BMI, Muscle Mass (Real dynamic calculations)
                    item {
                        Text(
                            text = "Body Composition Stats",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        )
                    }

                    item {
                        GymCard(backgroundColor = PrimaryCard) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                val bfVal = if (state.latestBodyFat != null) "${state.latestBodyFat}%" else "--"
                                val bfDelta = if (state.latestBodyFat != null) "Clinical/Measured" else "Tap Log"
                                BodyStatDetailRow(
                                    label = "Body Fat %",
                                    value = bfVal,
                                    delta = bfDelta,
                                    deltaColor = AccentSuccess
                                )

                                androidx.compose.material3.HorizontalDivider(color = DividerDark)

                                val bmiVal = if (state.latestBmi != null) "${state.latestBmi}" else "--"
                                val bmiDelta = state.latestBmiCategory ?: (if (state.heightCm == null) "Set height in Profile" else "Normal")
                                BodyStatDetailRow(
                                    label = "BMI (Body Mass Index)",
                                    value = bmiVal,
                                    delta = bmiDelta,
                                    deltaColor = if (state.latestBmiCategory == "Normal") AccentSuccess else NeonAmber
                                )

                                androidx.compose.material3.HorizontalDivider(color = DividerDark)

                                val mmVal = if (state.latestMuscleMass != null) "${state.latestMuscleMass} ${state.preferredUnit}" else "--"
                                val mmDelta = if (state.latestMuscleMass != null) "Lean Mass" else "Derived"
                                BodyStatDetailRow(
                                    label = "Muscle Mass (Lean Body Mass)",
                                    value = mmVal,
                                    delta = mmDelta,
                                    deltaColor = NeonCyan
                                )
                            }
                        }
                    }

                    // Educational Guide explaining the exact tracking math
                    item {
                        GymCard(backgroundColor = SecondaryCard) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = null,
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "How Body Composition is Tracked:",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryText
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• BMI = Weight (kg) ÷ Height (m)² using your profile height.\n• Body Fat % = Entered directly from your smart scale or caliper, or estimated using the clinical Deurenberg anthropometric formula.\n• Muscle Mass = Total Weight × (100% - Body Fat %), giving your exact Lean Body Mass (LBM) in real time.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SecondaryText,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }
                    }

                    item {
                        GymGradientButton(
                            text = "+ Log Body Composition",
                            onClick = { showAddWeightDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showAddWeightDialog) {
        AddWeightDialog(
            unit = state.preferredUnit,
            heightCm = state.heightCm,
            age = state.userAge,
            onConfirm = { weight, bodyFat, muscleMass, notes ->
                viewModel.addBodyWeight(weight, bodyFat, muscleMass, notes)
                showAddWeightDialog = false
            },
            onDismiss = { showAddWeightDialog = false }
        )
    }
}

@Composable
fun OverviewStatPill(
    title: String,
    value: String,
    delta: String,
    modifier: Modifier = Modifier
) {
    GymCard(
        backgroundColor = PrimaryCard,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = MutedText)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AccentSuccess.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = delta,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentSuccess,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BodyStatDetailRow(
    label: String,
    value: String,
    delta: String,
    deltaColor: Color = AccentSuccess
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryText))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = delta, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = deltaColor))
        }
    }
}
