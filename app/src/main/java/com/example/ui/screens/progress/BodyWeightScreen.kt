package com.example.ui.screens.progress

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BodyWeightEntry
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.GymCard
import com.example.ui.components.GymGradientButton
import com.example.ui.components.GymTextField
import com.example.ui.components.StatCard
import com.example.ui.components.WeightLineChart
import com.example.ui.theme.AccentError
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.AccentWarning
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryCard
import com.example.ui.theme.SecondaryText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyWeightScreen(
    viewModel: ProgressViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<BodyWeightEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Body Weight Tracker") },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryPurple,
                contentColor = PrimaryText,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("log_body_weight_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Body Weight")
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundDark)
                .testTag("body_weight_screen"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Row: Current vs Previous
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Current Weight",
                        value = if (state.latestWeight != null) "${state.latestWeight} ${state.preferredUnit}" else "--",
                        subtitle = "latest measurement",
                        icon = Icons.Default.MonitorWeight,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Previous Weight",
                        value = if (state.previousWeight != null) "${state.previousWeight} ${state.preferredUnit}" else "--",
                        subtitle = if (state.weightDelta != null) {
                            val sign = if (state.weightDelta!! > 0) "+" else ""
                            "$sign${String.format("%.1f", state.weightDelta)} ${state.preferredUnit}"
                        } else "no prior entry",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Stats Row 2: BMI & Body Composition (Body Fat %, Muscle Mass)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "BMI",
                        value = if (state.latestBmi != null) "${state.latestBmi}" else "--",
                        subtitle = state.latestBmiCategory ?: if (state.heightCm == null) "Set height in Profile" else "BMI",
                        icon = Icons.Default.TrendingUp,
                        accentColor = AccentSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Body Fat & Muscle",
                        value = if (state.latestBodyFat != null) "${state.latestBodyFat}%" else "--",
                        subtitle = if (state.latestMuscleMass != null) "${state.latestMuscleMass} ${state.preferredUnit} lean mass" else "Fat & muscle stats",
                        icon = Icons.Default.FitnessCenter,
                        accentColor = PrimaryPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Weight Chart
            item {
                WeightLineChart(
                    points = state.weightChartData,
                    unit = state.preferredUnit
                )
            }

            // Educational / Medical Guidance Banner
            item {
                GymCard(backgroundColor = SecondaryCard) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "How Body Composition is Tracked",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• BMI: Calculated automatically as Weight (kg) ÷ Height (m)² using your profile height.\n• Body Fat %: Logged directly from your smart scale or caliper, or estimated clinically (Deurenberg method).\n• Muscle Mass: Computed as Total Weight × (100% - Body Fat %), tracking your genuine Lean Body Mass progress.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SecondaryText,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }

            // History Header
            item {
                Text(
                    text = "MEASUREMENT HISTORY (${state.weightEntries.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MutedText,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            if (state.weightEntries.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.MonitorWeight,
                        title = "No Weights Logged",
                        message = "Start tracking your body weight measurements to monitor progress over time.",
                        actionButtonText = "Log First Weight",
                        onActionClick = { showAddDialog = true }
                    )
                }
            } else {
                items(state.weightEntries) { entry ->
                    BodyWeightListItem(
                        entry = entry,
                        unit = state.preferredUnit,
                        heightCm = state.heightCm,
                        age = state.userAge,
                        onDelete = { entryToDelete = entry }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddWeightDialog(
            unit = state.preferredUnit,
            heightCm = state.heightCm,
            age = state.userAge,
            onConfirm = { weight, bodyFat, muscleMass, notes ->
                viewModel.addBodyWeight(weight, bodyFat, muscleMass, notes)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    entryToDelete?.let { entry ->
        ConfirmationDialog(
            show = true,
            title = "Delete Measurement?",
            message = "Are you sure you want to delete the entry of ${entry.weight} ${entry.weightUnit}?",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteBodyWeight(entry.id)
                entryToDelete = null
            },
            onDismiss = { entryToDelete = null }
        )
    }
}

@Composable
fun BodyWeightListItem(
    entry: BodyWeightEntry,
    unit: String,
    heightCm: Double? = null,
    age: Int? = null,
    onDelete: () -> Unit
) {
    val dateStr = remember(entry.date) {
        val sdf = SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(entry.date))
    }

    val bmi = remember(entry, heightCm) { entry.calculateBmi(heightCm) }
    val bmiCategory = remember(entry, heightCm) { entry.getBmiCategory(heightCm) }
    val bodyFat = remember(entry, heightCm, age) { entry.calculateBodyFatPercentage(heightCm, age) }
    val muscleMass = remember(entry, heightCm, age) { entry.calculateMuscleMass(heightCm, age) }

    GymCard(
        backgroundColor = PrimaryCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${entry.weight} ${entry.weightUnit}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    )

                    if (bmi != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryPurple.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "BMI $bmi • ${bmiCategory ?: ""}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PrimaryPurple,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Body Composition badges
                if (bodyFat != null || muscleMass != null) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (bodyFat != null) {
                            Text(
                                text = "Body Fat: $bodyFat%",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = AccentSuccess,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        if (muscleMass != null) {
                            Text(
                                text = "Muscle: $muscleMass $unit",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                )

                if (!entry.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete measurement", tint = AccentError)
            }
        }
    }
}

@Composable
fun AddWeightDialog(
    unit: String,
    heightCm: Double? = null,
    age: Int? = null,
    onConfirm: (weight: Double, bodyFat: Double?, muscleMass: Double?, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var weightInput by remember { mutableStateOf("") }
    var bodyFatInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val parsedWeight = weightInput.toDoubleOrNull()
    val parsedBodyFat = bodyFatInput.toDoubleOrNull()

    // Real-time calculation previews
    val previewBmi = remember(parsedWeight, heightCm) {
        if (parsedWeight != null && parsedWeight > 0.0 && heightCm != null && heightCm > 0.0) {
            val weightInKg = if (unit.equals("lb", ignoreCase = true)) parsedWeight * 0.45359237 else parsedWeight
            val hM = heightCm / 100.0
            val b = weightInKg / (hM * hM)
            (b * 10.0).toInt() / 10.0
        } else null
    }

    val previewEstimatedBf = remember(previewBmi, parsedBodyFat, age) {
        if (parsedBodyFat != null && parsedBodyFat > 0.0) {
            parsedBodyFat
        } else if (previewBmi != null) {
            val userAge = age ?: 25
            val est = (1.20 * previewBmi) + (0.23 * userAge) - 16.2
            ((est.coerceIn(5.0, 50.0)) * 10.0).toInt() / 10.0
        } else null
    }

    val previewMuscleMass = remember(parsedWeight, previewEstimatedBf) {
        if (parsedWeight != null && previewEstimatedBf != null) {
            val mass = parsedWeight * (1.0 - (previewEstimatedBf / 100.0))
            (mass * 10.0).toInt() / 10.0
        } else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Body Composition",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryText)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GymTextField(
                    value = weightInput,
                    onValueChange = {
                        weightInput = it
                        error = null
                    },
                    label = "Weight ($unit) *",
                    placeholder = "e.g. 75.5",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    errorMessage = error,
                    modifier = Modifier.fillMaxWidth().testTag("weight_dialog_input")
                )

                GymTextField(
                    value = bodyFatInput,
                    onValueChange = { bodyFatInput = it },
                    label = "Body Fat % (Optional)",
                    placeholder = if (previewEstimatedBf != null) "Est: $previewEstimatedBf% or enter measured" else "e.g. 14.5",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Live dynamic calculations preview card
                if (parsedWeight != null && parsedWeight > 0) {
                    GymCard(backgroundColor = SecondaryCard) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "CALCULATED BODY METRICS",
                                style = MaterialTheme.typography.labelSmall.copy(color = PrimaryPurple, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("BMI: ${previewBmi ?: "--"}", style = MaterialTheme.typography.bodySmall.copy(color = PrimaryText, fontWeight = FontWeight.Bold))
                                Text("Body Fat: ${previewEstimatedBf?.let { "$it%" } ?: "--"}", style = MaterialTheme.typography.bodySmall.copy(color = AccentSuccess, fontWeight = FontWeight.Bold))
                                Text("Muscle: ${previewMuscleMass?.let { "$it $unit" } ?: "--"}", style = MaterialTheme.typography.bodySmall.copy(color = NeonCyan, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }

                GymTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = "Notes (Optional)",
                    placeholder = "Morning fasted, caliper check, etc.",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            GymGradientButton(
                text = "Save Measurement",
                onClick = {
                    val w = weightInput.toDoubleOrNull()
                    if (w == null || w <= 0.0) {
                        error = "Enter a valid positive weight value"
                    } else {
                        val bf = bodyFatInput.toDoubleOrNull() ?: previewEstimatedBf
                        val mm = previewMuscleMass
                        onConfirm(w, bf, mm, notesInput.trim().ifEmpty { null })
                    }
                },
                testTag = "save_weight_dialog_btn"
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SecondaryText)
            }
        },
        containerColor = PrimaryCard,
        shape = RoundedCornerShape(16.dp)
    )
}

// Backward compatible overload for simple call sites
@Composable
fun AddWeightDialog(
    unit: String,
    onConfirm: (Double, String?) -> Unit,
    onDismiss: () -> Unit
) {
    AddWeightDialog(
        unit = unit,
        heightCm = null,
        age = null,
        onConfirm = { weight, _, _, notes -> onConfirm(weight, notes) },
        onDismiss = onDismiss
    )
}
