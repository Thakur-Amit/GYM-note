package com.example.ui.screens.body

import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BodyMeasurementEntry
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.GymCard
import com.example.ui.components.GymGradientButton
import com.example.ui.components.GymSecondaryButton
import com.example.ui.theme.AccentError
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.DividerDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryCard
import com.example.ui.theme.SecondaryText
import com.example.util.BodyMath
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMeasurementsScreen(
    viewModel: BodyMeasurementsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showInfoDialog by remember { mutableStateOf(false) }

    val tabTitles = listOf("Overview", "History", "Charts", "Add New")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Body Measurements",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryText
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = SecondaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = PrimaryText
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundDark)
                .testTag("body_measurements_screen")
        ) {
            // Pill Navigation Tabs matching reference UI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) PrimaryPurple else SecondaryCard)
                            .clickable { selectedTabIndex = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PrimaryText else SecondaryText,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> OverviewTabContent(
                    state = state,
                    onAddNewClick = { selectedTabIndex = 3 },
                    onViewAllClick = { selectedTabIndex = 1 }
                )
                1 -> HistoryTabContent(
                    state = state,
                    onDelete = { id -> viewModel.deleteMeasurement(id) },
                    onAddNewClick = { selectedTabIndex = 3 }
                )
                2 -> ChartsTabContent(
                    state = state,
                    onMetricSelected = { viewModel.setChartMetric(it) },
                    onTimeRangeSelected = { viewModel.setTimeRange(it) }
                )
                3 -> AddNewTabContent(
                    state = state,
                    onSave = { weight, date, waist, neck, hips, chest, shoulders, lArm, rArm, lForearm, rForearm, lThigh, rThigh, lCalf, rCalf, notes ->
                        viewModel.saveNewMeasurement(
                            weightKg = weight,
                            measuredAt = date,
                            waistCm = waist,
                            neckCm = neck,
                            hipsCm = hips,
                            chestCm = chest,
                            shouldersCm = shoulders,
                            leftArmCm = lArm,
                            rightArmCm = rArm,
                            leftForearmCm = lForearm,
                            rightForearmCm = rForearm,
                            leftThighCm = lThigh,
                            rightThighCm = rThigh,
                            leftCalfCm = lCalf,
                            rightCalfCm = rCalf,
                            notes = notes
                        ) {
                            Toast.makeText(context, "Body measurements saved successfully!", Toast.LENGTH_SHORT).show()
                            selectedTabIndex = 0
                        }
                    }
                )
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("About Body Tracking", color = PrimaryText, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Track your body circumferences, weight in kg, and body composition over time. Body Fat % is estimated using the validated U.S. Navy circumference method.",
                    color = SecondaryText
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("OK", color = PrimaryPurple)
                }
            },
            containerColor = PrimaryCard
        )
    }
}

@Composable
private fun OverviewTabContent(
    state: BodyMeasurementsUiState,
    onAddNewClick: () -> Unit,
    onViewAllClick: () -> Unit
) {
    val lastDateStr = remember(state.latestMeasurement) {
        state.latestMeasurement?.let {
            SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(it.measuredAt))
        } ?: "No records yet"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Last Updated Card with + Add New action
        item {
            GymCard(
                backgroundColor = PrimaryCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElevatedSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Last Updated",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SecondaryText,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = lastDateStr,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }

                    GymGradientButton(
                        text = "+ Add New",
                        onClick = onAddNewClick,
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        }

        // 2. 3 KPI Summary Cards (Weight, Body Fat %, BMI)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Weight Card
                val weightVal = state.latestWeight?.weight
                OverviewKpiCard(
                    title = "Weight",
                    value = if (weightVal != null) "${String.format(Locale.getDefault(), "%.1f", weightVal)} kg" else "--",
                    deltaStr = state.weightDelta.deltaSimple,
                    unit = "kg",
                    modifier = Modifier.weight(1f)
                )

                // Body Fat Card
                val bfVal = state.latestComposition?.bodyFatPercentEstimate ?: state.latestWeight?.bodyFatPercentage
                OverviewKpiCard(
                    title = "Body Fat (Est.)",
                    value = if (bfVal != null) "${String.format(Locale.getDefault(), "%.1f", bfVal)} %" else "--",
                    deltaStr = state.bodyFatDelta.deltaSimple,
                    unit = "%",
                    modifier = Modifier.weight(1f)
                )

                // BMI Card
                val bmiVal = state.latestComposition?.bmiValue ?: state.latestWeight?.calculateBmi(state.userProfile.heightCm)
                OverviewKpiCard(
                    title = "BMI",
                    value = if (bmiVal != null) String.format(Locale.getDefault(), "%.1f", bmiVal) else "--",
                    deltaStr = state.bmiDelta.deltaSimple,
                    unit = "",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Body Measurements Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Body Measurements",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = "View All >",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = PrimaryPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }
        }

        // 4. Centerpiece Body Figure Visual
        item {
            BodyFigureOverview(state = state)
        }

        // 5. Recent Records Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Records",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = "View All >",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = PrimaryPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }
        }

        val recentList = state.allMeasurements.take(3)
        if (recentList.isEmpty()) {
            item {
                GymCard(backgroundColor = PrimaryCard) {
                    Box(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "No measurement records saved yet. Tap '+ Add New' above to record your first set of measurements.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                        )
                    }
                }
            }
        } else {
            items(recentList) { record ->
                val dateStr = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(record.measuredAt))
                val matchingWeight = state.allWeights.firstOrNull { Math.abs(it.date - record.measuredAt) < 24 * 3600 * 1000L }

                GymCard(
                    backgroundColor = PrimaryCard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewAllClick() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPurple)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val wStr = if (matchingWeight != null) "${String.format(Locale.getDefault(), "%.1f", matchingWeight.weight)} kg • " else ""
                                Text(
                                    text = "${wStr}Waist ${record.waistCm ?: "--"} cm • Chest ${record.chestCm ?: "--"} cm",
                                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewKpiCard(
    title: String,
    value: String,
    deltaStr: String?,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PrimaryCard)
            .padding(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = SecondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText,
                fontSize = 17.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (deltaStr != null) {
            Text(
                text = "$deltaStr $unit",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = NeonEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
            Text(
                text = "vs last record",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MutedText,
                    fontSize = 9.sp
                )
            )
        } else {
            Text(
                text = "Baseline",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MutedText,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun HistoryTabContent(
    state: BodyMeasurementsUiState,
    onDelete: (Long) -> Unit,
    onAddNewClick: () -> Unit
) {
    var entryToDeleteId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (state.allMeasurements.isEmpty()) {
            item {
                GymCard(backgroundColor = PrimaryCard) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Measurement Records",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryText)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add your first body measurement entry to see progress history and circumference changes.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        GymGradientButton(text = "Add Measurements", onClick = onAddNewClick)
                    }
                }
            }
        } else {
            items(state.allMeasurements) { entry ->
                val dateStr = SimpleDateFormat("d MMMM yyyy, h:mm a", Locale.getDefault()).format(Date(entry.measuredAt))
                val matchingWeight = state.allWeights.firstOrNull { Math.abs(it.date - entry.measuredAt) < 24 * 3600 * 1000L }

                GymCard(backgroundColor = PrimaryCard, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText,
                                        fontSize = 15.sp
                                    )
                                )
                                if (matchingWeight != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Weight: ${matchingWeight.weight} kg",
                                        style = MaterialTheme.typography.bodySmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            IconButton(onClick = { entryToDeleteId = entry.id }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = AccentError.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Circumferences grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                entry.chestCm?.let { Text("Chest: $it cm", style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)) }
                                entry.waistCm?.let { Text("Waist: $it cm", style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)) }
                                entry.hipsCm?.let { Text("Hips: $it cm", style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)) }
                                entry.neckCm?.let { Text("Neck: $it cm", style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)) }
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                entry.shouldersCm?.let { Text("Shoulders: $it cm", style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)) }
                                entry.leftArmCm?.let { Text("L Arm: $it cm", style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)) }
                                entry.rightArmCm?.let { Text("R Arm: $it cm", style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)) }
                                entry.leftThighCm?.let { Text("L Thigh: $it cm", style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)) }
                            }
                        }

                        if (!entry.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Note: ${entry.notes}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
                            )
                        }
                    }
                }
            }
        }
    }

    entryToDeleteId?.let { id ->
        ConfirmationDialog(
            show = true,
            title = "Delete Measurement Entry?",
            message = "Are you sure you want to delete this historical record? This cannot be undone.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                onDelete(id)
                entryToDeleteId = null
            },
            onDismiss = { entryToDeleteId = null }
        )
    }
}

@Composable
private fun ChartsTabContent(
    state: BodyMeasurementsUiState,
    onMetricSelected: (String) -> Unit,
    onTimeRangeSelected: (String) -> Unit
) {
    var expandedMetricDropdown by remember { mutableStateOf(false) }

    val metrics = listOf(
        "Weight", "Body Fat (Est.)", "BMI", "Neck", "Chest", "Waist",
        "Hips", "Left Arm", "Right Arm", "Shoulders", "Left Thigh", "Right Thigh",
        "Left Calf", "Right Calf"
    )
    val timeRanges = listOf("1M", "3M", "6M", "1Y", "All")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dropdown Metric Selector & Time Range Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metric Selector Dropdown Box
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SecondaryCard)
                            .clickable { expandedMetricDropdown = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.selectedChartMetric,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "▾", color = SecondaryText)
                    }

                    DropdownMenu(
                        expanded = expandedMetricDropdown,
                        onDismissRequest = { expandedMetricDropdown = false },
                        modifier = Modifier.background(PrimaryCard)
                    ) {
                        metrics.forEach { metric ->
                            DropdownMenuItem(
                                text = { Text(metric, color = PrimaryText) },
                                onClick = {
                                    onMetricSelected(metric)
                                    expandedMetricDropdown = false
                                }
                            )
                        }
                    }
                }

                // Time Range Pills
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    timeRanges.forEach { range ->
                        val isSelected = state.selectedTimeRange == range
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryPurple else SecondaryCard)
                                .clickable { onTimeRangeSelected(range) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = range,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PrimaryText else SecondaryText,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Big Trend Chart Card
        item {
            GymCard(
                backgroundColor = PrimaryCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${state.selectedChartMetric} Trend",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText,
                                fontSize = 17.sp
                            )
                        )
                        if (state.chartTotalChange.isNotBlank()) {
                            Text(
                                text = state.chartTotalChange,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (state.chartIsDecreased == true) NeonEmerald else NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Smooth Canvas Trend Graph
                    if (state.chartPoints.size >= 2) {
                        BodyTrendCanvas(
                            points = state.chartPoints,
                            metric = state.selectedChartMetric,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Not enough data points for this time range. Add at least 2 measurements.",
                                style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                            )
                        }
                    }
                }
            }
        }

        // All Measurements Progress Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Measurements Progress",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        fontSize = 18.sp
                    )
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // List of all measurement items with sparklines
        items(state.progressItems) { item ->
            GymCard(
                backgroundColor = PrimaryCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElevatedSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Straighten,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText,
                                fontSize = 14.sp
                            )
                        )
                    }

                    // Value and Delta
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text(
                            text = item.currentFormatted,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.changeFormatted,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (item.isDecreased == true) NeonEmerald else NeonCyan,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Mini Sparkline Graph
                    MiniSparkline(
                        values = item.historyPoints,
                        modifier = Modifier
                            .width(60.dp)
                            .height(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BodyTrendCanvas(
    points: List<MetricRecord>,
    metric: String,
    modifier: Modifier = Modifier
) {
    val minVal = points.minOf { it.value }
    val maxVal = points.maxOf { it.value }
    val range = if (maxVal - minVal > 0.0) maxVal - minVal else 1.0

    val lastPoint = points.last()

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val padding = 20f

            val chartWidth = width - (padding * 2)
            val chartHeight = height - (padding * 2)

            val stepX = chartWidth / (points.size - 1).coerceAtLeast(1)

            val path = Path()
            val fillPath = Path()

            val canvasPoints = points.mapIndexed { index, p ->
                val x = padding + (index * stepX)
                val normY = ((p.value - minVal) / range).toFloat()
                val y = padding + (chartHeight * (1f - normY))
                Offset(x, y)
            }

            canvasPoints.forEachIndexed { index, pt ->
                if (index == 0) {
                    path.moveTo(pt.x, pt.y)
                    fillPath.moveTo(pt.x, height)
                    fillPath.lineTo(pt.x, pt.y)
                } else {
                    val prev = canvasPoints[index - 1]
                    val cx = (prev.x + pt.x) / 2
                    path.cubicTo(cx, prev.y, cx, pt.y, pt.x, pt.y)
                    fillPath.cubicTo(cx, prev.y, cx, pt.y, pt.x, pt.y)
                }
            }

            fillPath.lineTo(canvasPoints.last().x, height)
            fillPath.close()

            // Draw Area Gradient Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryPurple.copy(alpha = 0.45f),
                        PrimaryPurple.copy(alpha = 0.05f)
                    )
                )
            )

            // Draw Line
            drawPath(
                path = path,
                color = PrimaryPurple,
                style = Stroke(width = 4f)
            )

            // Draw points
            canvasPoints.forEach { pt ->
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = pt
                )
            }

            // Highlight latest point
            val lastPt = canvasPoints.last()
            drawCircle(
                color = PrimaryPurple,
                radius = 7f,
                center = lastPt
            )
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = lastPt
            )
        }

        // Callout Pill for Latest Value
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E1F2E))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${String.format(Locale.getDefault(), "%.1f", lastPoint.value)} ${if (metric == "Weight") "kg" else "cm"}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = PrimaryText)
                )
                Text(
                    text = SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(lastPoint.dateMillis)),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = SecondaryText)
                )
            }
        }
    }
}

@Composable
fun MiniSparkline(
    values: List<Double>,
    modifier: Modifier = Modifier
) {
    if (values.size < 2) {
        Box(modifier = modifier)
        return
    }

    val min = values.minOrNull() ?: 0.0
    val max = values.maxOrNull() ?: 1.0
    val range = if (max - min > 0.0) max - min else 1.0

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val step = w / (values.size - 1)
        val path = Path()
        val fillPath = Path()

        values.forEachIndexed { i, v ->
            val x = i * step
            val norm = ((v - min) / range).toFloat()
            val y = h - (norm * h)

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(w, h)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(PrimaryPurple.copy(alpha = 0.35f), Color.Transparent)
            )
        )
        drawPath(
            path = path,
            color = PrimaryPurple,
            style = Stroke(width = 2.5f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNewTabContent(
    state: BodyMeasurementsUiState,
    onSave: (
        weight: Double?, date: Long, waist: Double?, neck: Double?, hips: Double?,
        chest: Double?, shoulders: Double?, lArm: Double?, rArm: Double?,
        lForearm: Double?, rForearm: Double?, lThigh: Double?, rThigh: Double?,
        lCalf: Double?, rCalf: Double?, notes: String?
    ) -> Unit
) {
    var measuredDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var weightInput by remember { mutableStateOf("") }
    var waistInput by remember { mutableStateOf("") }
    var neckInput by remember { mutableStateOf("") }
    var hipsInput by remember { mutableStateOf("") }
    var chestInput by remember { mutableStateOf("") }
    var shouldersInput by remember { mutableStateOf("") }
    var lArmInput by remember { mutableStateOf("") }
    var rArmInput by remember { mutableStateOf("") }
    var lThighInput by remember { mutableStateOf("") }
    var rThighInput by remember { mutableStateOf("") }
    var lCalfInput by remember { mutableStateOf("") }
    var rCalfInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    val dateStr = remember(measuredDateMillis) {
        SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(measuredDateMillis))
    }

    // Live preview calculation
    val wKg = weightInput.toDoubleOrNull()
    val waist = waistInput.toDoubleOrNull()
    val neck = neckInput.toDoubleOrNull()
    val hips = hipsInput.toDoubleOrNull()

    val liveBmi = remember(wKg) {
        if (wKg != null) BodyMath.calculateBmi(wKg, state.userProfile.heightCm ?: 175.0) else null
    }
    val liveBf = remember(waist, neck, hips) {
        if (waist != null && neck != null) {
            BodyMath.calculateNavyBodyFatPercentage(
                heightCm = state.userProfile.heightCm ?: 175.0,
                waistCm = waist,
                neckCm = neck,
                hipCm = hips,
                sex = state.userProfile.gender
            )
        } else null
    }
    val liveFfm = remember(wKg, liveBf) {
        if (wKg != null && liveBf != null) BodyMath.calculateFatFreeMass(wKg, liveBf) else null
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Card
        item {
            GymCard(backgroundColor = PrimaryCard) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("MEASUREMENT DATE", style = MaterialTheme.typography.labelSmall.copy(color = PrimaryPurple, fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(dateStr, style = MaterialTheme.typography.titleMedium.copy(color = PrimaryText, fontWeight = FontWeight.Bold))
                    }
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = SecondaryText)
                }
            }
        }

        // Live Calculated Preview Card
        if (liveBmi != null || liveBf != null) {
            item {
                GymCard(backgroundColor = SecondaryCard) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        liveBmi?.let {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BMI", style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText))
                                Text("$it", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NeonCyan))
                            }
                        }
                        liveBf?.let {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Body Fat (Est.)", style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText))
                                Text("$it %", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NeonEmerald))
                            }
                        }
                        liveFfm?.let {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Fat-Free Mass", style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText))
                                Text("$it kg", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryPurple))
                            }
                        }
                    }
                }
            }
        }

        // Weight Section
        item {
            GymCard(backgroundColor = PrimaryCard) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("BODY WEIGHT", style = MaterialTheme.typography.labelSmall.copy(color = PrimaryPurple, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = DividerDark
                        )
                    )
                }
            }
        }

        // Core Measurements Section
        item {
            GymCard(backgroundColor = PrimaryCard) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("CORE CIRCUMFERENCES (CM)", style = MaterialTheme.typography.labelSmall.copy(color = PrimaryPurple, fontWeight = FontWeight.Bold))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = waistInput,
                            onValueChange = { waistInput = it },
                            label = { Text("Waist (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = DividerDark)
                        )
                        OutlinedTextField(
                            value = neckInput,
                            onValueChange = { neckInput = it },
                            label = { Text("Neck (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = DividerDark)
                        )
                    }
                    OutlinedTextField(
                        value = hipsInput,
                        onValueChange = { hipsInput = it },
                        label = { Text("Hips (cm) - Required for women") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = DividerDark)
                    )
                }
            }
        }

        // Upper & Lower Body Measurements
        item {
            GymCard(backgroundColor = PrimaryCard) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("UPPER & LOWER BODY (CM)", style = MaterialTheme.typography.labelSmall.copy(color = PrimaryPurple, fontWeight = FontWeight.Bold))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = chestInput,
                            onValueChange = { chestInput = it },
                            label = { Text("Chest (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = DividerDark)
                        )
                        OutlinedTextField(
                            value = shouldersInput,
                            onValueChange = { shouldersInput = it },
                            label = { Text("Shoulders (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = DividerDark)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = lArmInput,
                            onValueChange = { lArmInput = it },
                            label = { Text("Left Arm (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = DividerDark)
                        )
                        OutlinedTextField(
                            value = rArmInput,
                            onValueChange = { rArmInput = it },
                            label = { Text("Right Arm (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = DividerDark)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = lThighInput,
                            onValueChange = { lThighInput = it },
                            label = { Text("Left Thigh (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = DividerDark)
                        )
                        OutlinedTextField(
                            value = rThighInput,
                            onValueChange = { rThighInput = it },
                            label = { Text("Right Thigh (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = DividerDark)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = lCalfInput,
                            onValueChange = { lCalfInput = it },
                            label = { Text("Left Calf (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = DividerDark)
                        )
                        OutlinedTextField(
                            value = rCalfInput,
                            onValueChange = { rCalfInput = it },
                            label = { Text("Right Calf (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = DividerDark)
                        )
                    }
                }
            }
        }

        // Notes Input
        item {
            GymCard(backgroundColor = PrimaryCard) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SESSION NOTES", style = MaterialTheme.typography.labelSmall.copy(color = PrimaryPurple, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryPurple, unfocusedBorderColor = DividerDark)
                    )
                }
            }
        }

        // Save Button
        item {
            GymGradientButton(
                text = "Save Measurement Record",
                icon = Icons.Default.Check,
                onClick = {
                    onSave(
                        wKg,
                        measuredDateMillis,
                        waist,
                        neck,
                        hips,
                        chestInput.toDoubleOrNull(),
                        shouldersInput.toDoubleOrNull(),
                        lArmInput.toDoubleOrNull(),
                        rArmInput.toDoubleOrNull(),
                        null,
                        null,
                        lThighInput.toDoubleOrNull(),
                        rThighInput.toDoubleOrNull(),
                        lCalfInput.toDoubleOrNull(),
                        rCalfInput.toDoubleOrNull(),
                        notesInput.ifBlank { null }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = measuredDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { measuredDateMillis = it }
                    showDatePicker = false
                }) { Text("OK", color = PrimaryPurple) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = SecondaryText) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
