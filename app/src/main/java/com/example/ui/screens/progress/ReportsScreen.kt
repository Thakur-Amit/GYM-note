package com.example.ui.screens.progress

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GymCard
import com.example.ui.components.MuscleDistributionChart
import com.example.ui.components.StatCard
import com.example.ui.theme.AccentError
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.AccentWarning
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.DividerDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryCard
import com.example.ui.theme.SecondaryText
import java.text.DateFormatSymbols
import java.util.Calendar
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ProgressViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.reportsState.collectAsStateWithLifecycle()
    val current = state.currentMonthSummary
    val prev = state.previousMonthSummary

    val monthNames = DateFormatSymbols.getInstance().months
    val currentMonthName = monthNames.getOrNull(state.selectedMonth) ?: "Month"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Reports") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = PrimaryText,
                    navigationIconContentColor = PrimaryText
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundDark)
                .testTag("reports_screen"),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month & Year Selector
            item {
                GymCard(backgroundColor = PrimaryCard) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (state.selectedMonth > 0) {
                                    viewModel.setReportMonth(state.selectedMonth - 1)
                                } else {
                                    viewModel.setReportYear(state.selectedYear - 1)
                                    viewModel.setReportMonth(11)
                                }
                            }
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = PrimaryText)
                        }

                        Text(
                            text = "$currentMonthName ${state.selectedYear}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        )

                        IconButton(
                            onClick = {
                                if (state.selectedMonth < 11) {
                                    viewModel.setReportMonth(state.selectedMonth + 1)
                                } else {
                                    viewModel.setReportYear(state.selectedYear + 1)
                                    viewModel.setReportMonth(0)
                                }
                            }
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = PrimaryText)
                        }
                    }
                }
            }

            // Month Overview Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Completed Workouts",
                        value = "${current?.completedWorkoutsCount ?: 0}",
                        subtitle = "vs ${prev?.completedWorkoutsCount ?: 0} last month",
                        icon = Icons.Default.FitnessCenter,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Volume",
                        value = "${current?.totalVolume?.roundToInt() ?: 0} kg",
                        subtitle = if (state.volumeDeltaPercent != null) {
                            val sign = if (state.volumeDeltaPercent!! >= 0) "+" else ""
                            "$sign${String.format("%.1f", state.volumeDeltaPercent)}% vs prev"
                        } else "baseline month",
                        icon = Icons.Default.ShowChart,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Sets Completed",
                        value = "${current?.completedSetsCount ?: 0}",
                        icon = Icons.Default.Repeat,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Workout Consistency",
                        value = "${current?.consistencyRatePercent ?: 0}%",
                        icon = Icons.Default.Timeline,
                        accentColor = AccentSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Period over Period Comparison Card
            item {
                GymCard(backgroundColor = PrimaryCard) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "MONTH-OVER-MONTH PERFORMANCE DELTA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val delta = state.volumeDeltaPercent
                        if (delta != null) {
                            val isPositive = delta >= 0
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isPositive) AccentSuccess else AccentWarning,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${if (isPositive) "+" else ""}${String.format("%.1f", delta)}% training volume compared to prior month",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = PrimaryText,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        } else {
                            Text(
                                text = "Comparison will display when both current and previous months contain logged workouts.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                            )
                        }
                    }
                }
            }

            // Muscle Group Breakdown
            if (current != null && current.muscleGroupVolumeMap.isNotEmpty()) {
                item {
                    MuscleDistributionChart(
                        distributionMap = current.muscleGroupVolumeMap,
                        unit = "kg"
                    )
                }
            }

            // Educational / Scientific Distinctions
            item {
                GymCard(backgroundColor = SecondaryCard) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Training volume measures the total external mechanical work performed (load × reps). Progression in volume correlates with stimulus when executed with consistent technique, recovery, and adequate nutrition.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SecondaryText,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
