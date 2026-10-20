package com.example.ui.screens.exercises

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExercisePerformanceRecord
import com.example.data.model.QuickSetInput
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.ExerciseImage
import com.example.ui.components.ExerciseProgressChart
import com.example.ui.components.ExerciseSessionPoint
import com.example.ui.components.GymCard
import com.example.ui.components.GymGradientButton
import com.example.ui.components.GymSecondaryButton
import com.example.util.BodyMath
import com.example.ui.theme.AccentError
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.DividerDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryCard
import com.example.ui.theme.SecondaryText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class QuickSetRowState(
    val setNumber: Int,
    var weight: String = "",
    var reps: String = "",
    var isDone: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    viewModel: ExercisesViewModel,
    fromWorkout: Boolean = false,
    routineName: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    LaunchedEffect(exerciseId) {
        viewModel.loadExerciseDetails(exerciseId)
    }

    val state by viewModel.detailState.collectAsStateWithLifecycle()
    val exercise = state.exercise
    var showArchiveDialog by remember { mutableStateOf(false) }
    var preferredUnit by remember { mutableStateOf("kg") }
    var selectedHistoryRange by remember { mutableStateOf("1M") }
    var saveFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // When fromWorkout is false (standalone Exercises tab), ONLY show History and About!
    // When fromWorkout is true (Workout routine drilldown), show Log, History, About!
    val tabs = if (fromWorkout) listOf("Log", "History", "About") else listOf("History", "About")
    var selectedTabIndex by remember(fromWorkout) { mutableStateOf(0) }
    val currentTab = tabs[selectedTabIndex.coerceIn(0, tabs.size - 1)]

    // Sets start clean and empty ready to enter weights & reps
    val setsList = remember {
        mutableStateListOf(
            QuickSetRowState(1, "", "", false),
            QuickSetRowState(2, "", "", false),
            QuickSetRowState(3, "", "", false)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        if (fromWorkout && !routineName.isNullOrBlank()) {
                            Text(
                                text = routineName.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PrimaryPurple,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                        Text(
                            text = exercise?.name ?: "Exercise Details",
                            maxLines = 1,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
                    }
                },
                actions = {
                    if (exercise?.isCustom == true) {
                        IconButton(onClick = { onNavigateToEdit(exercise.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryPurple)
                        }
                    }
                    IconButton(onClick = { showArchiveDialog = true }) {
                        Icon(Icons.Default.Archive, contentDescription = "Archive", tint = SecondaryText)
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
        if (exercise == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading exercise...", color = SecondaryText)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundDark)
                .testTag("exercise_detail_screen")
        ) {
            // Segmented Pill Tabs: [Log] [History] [About] or [History] [About]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryCard)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) PrimaryPurple else Color.Transparent)
                            .clickable { selectedTabIndex = index },
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

            when (currentTab) {
                "Log" -> {
                    // TAB: LOG SCREEN (Only visible when navigated from a workout routine)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Hero Demonstration Image Preview
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(ElevatedSurface)
                            ) {
                                ExerciseImage(
                                    imagePath = exercise.imagePath,
                                    cornerRadius = 18.dp,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Real Previous Record Card
                        item {
                            val lastRecord = state.history.firstOrNull()
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
                                    Column {
                                        Text(
                                            text = "Previous Record",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MutedText)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (lastRecord != null) {
                                            val maxReps = lastRecord.sets.maxOfOrNull { it.reps ?: 0 } ?: 0
                                            Text(
                                                text = "${lastRecord.maxWeight.roundToInt()} $preferredUnit × $maxReps reps",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryText
                                                )
                                            )
                                        } else {
                                            Text(
                                                text = "No recorded sets yet",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = SecondaryText
                                                )
                                            )
                                        }
                                    }

                                    if (lastRecord != null) {
                                        val dateStr = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(lastRecord.date))
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                                        )
                                    } else {
                                        Text(
                                            text = "Ready to log",
                                            style = MaterialTheme.typography.bodySmall.copy(color = NeonCyan)
                                        )
                                    }
                                }
                            }
                        }

                        // Enter Sets Header + kg | lbs toggle
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Enter Sets",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    )
                                )

                                // kg | lbs toggle pill
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PrimaryCard)
                                        .padding(2.dp)
                                ) {
                                    Text(
                                        text = "kg",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (preferredUnit == "kg") FontWeight.Bold else FontWeight.Normal,
                                            color = if (preferredUnit == "kg") PrimaryText else MutedText
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (preferredUnit == "kg") ElevatedSurface else Color.Transparent)
                                            .clickable { preferredUnit = "kg" }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                    Text(
                                        text = "lbs",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (preferredUnit == "lbs") FontWeight.Bold else FontWeight.Normal,
                                            color = if (preferredUnit == "lbs") PrimaryText else MutedText
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (preferredUnit == "lbs") ElevatedSurface else Color.Transparent)
                                            .clickable { preferredUnit = "lbs" }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Clean Blank Set Rows with editable Weight, editable Reps, and Delete Set button
                        itemsIndexed(setsList) { index, setItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryCard)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Set ${setItem.setNumber}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    ),
                                    modifier = Modifier.width(46.dp)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Editable Weight Input TextField
                                OutlinedTextField(
                                    value = setItem.weight,
                                    onValueChange = { newWeight ->
                                        setsList[index] = setItem.copy(weight = newWeight)
                                    },
                                    placeholder = { Text("kg", color = MutedText, fontSize = 13.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = ElevatedSurface,
                                        unfocusedContainerColor = ElevatedSurface,
                                        focusedBorderColor = PrimaryPurple,
                                        unfocusedBorderColor = DividerDark,
                                        focusedTextColor = PrimaryText,
                                        unfocusedTextColor = PrimaryText
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("set_weight_input_${setItem.setNumber}")
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = "×",
                                    style = MaterialTheme.typography.titleMedium.copy(color = MutedText)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Editable Reps Input TextField
                                OutlinedTextField(
                                    value = setItem.reps,
                                    onValueChange = { newReps ->
                                        setsList[index] = setItem.copy(reps = newReps)
                                    },
                                    placeholder = { Text("reps", color = MutedText, fontSize = 13.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = ElevatedSurface,
                                        unfocusedContainerColor = ElevatedSurface,
                                        focusedBorderColor = PrimaryPurple,
                                        unfocusedBorderColor = DividerDark,
                                        focusedTextColor = PrimaryText,
                                        unfocusedTextColor = PrimaryText
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("set_reps_input_${setItem.setNumber}")
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Done completion toggle
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (setItem.isDone) NeonEmerald else ElevatedSurface)
                                        .clickable {
                                            setsList[index] = setItem.copy(isDone = !setItem.isDone)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = if (setItem.isDone) Color.Black else SecondaryText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Delete Set Button
                                IconButton(
                                    onClick = {
                                        if (setsList.size > 1) {
                                            setsList.removeAt(index)
                                            val reindexed = setsList.mapIndexed { i, s -> s.copy(setNumber = i + 1) }
                                            setsList.clear()
                                            setsList.addAll(reindexed)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Set",
                                        tint = AccentError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Add Set Button
                        item {
                            GymSecondaryButton(
                                text = "+ Add Set",
                                onClick = {
                                    val nextNumber = setsList.size + 1
                                    setsList.add(
                                        QuickSetRowState(
                                            setNumber = nextNumber,
                                            weight = "",
                                            reps = "",
                                            isDone = false
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_set_btn")
                            )
                        }

                        // Save Log & Finish Button
                        item {
                            GymGradientButton(
                                text = "Save Log & Finish Workout",
                                onClick = {
                                    val validSets = setsList.mapNotNull { s ->
                                        val w = s.weight.toDoubleOrNull()
                                        val r = s.reps.toIntOrNull()
                                        if (w != null && w > 0.0 && r != null && r > 0) {
                                            QuickSetInput(
                                                setNumber = s.setNumber,
                                                weight = w,
                                                reps = r,
                                                weightUnit = preferredUnit,
                                                isCompleted = true
                                            )
                                        } else null
                                    }

                                    if (validSets.isEmpty()) {
                                        saveFeedbackMessage = "Please enter valid weight and reps for at least one set before saving."
                                    } else {
                                        viewModel.logExerciseWorkout(
                                            exerciseId = exercise.id,
                                            sessionName = routineName ?: exercise.name,
                                            setsInput = validSets,
                                            onSaved = {
                                                val totalVolume = validSets.sumOf { (it.weight ?: 0.0) * (it.reps ?: 0) }
                                                saveFeedbackMessage = "Workout successfully logged! ${validSets.size} sets recorded with ${totalVolume.toInt()} $preferredUnit volume."
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_workout_log_btn")
                            )
                        }
                    }
                }
                "History" -> {
                    // TAB 2: HISTORY SCREEN (Matching Image 1 Screen 6)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Filter Pills: [1W] [1M] [3M] [6M] [1Y] [All]
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("1W", "1M", "3M", "6M", "1Y", "All").forEach { r ->
                                    val isSelected = selectedHistoryRange == r
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) PrimaryPurple else SecondaryCard)
                                            .clickable { selectedHistoryRange = r },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = r,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) PrimaryText else SecondaryText
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Real Dual-Series Progress Graph (Estimated 1RM + Max Weight)
                        item {
                            val chartPoints = remember(state.history) {
                                state.history.mapNotNull { record ->
                                    val maxWeight = record.maxWeight
                                    val bestSet = record.sets.maxByOrNull { set ->
                                        BodyMath.calculateEpley1RM(set.weight ?: 0.0, set.reps ?: 0)
                                    }
                                    val best1RM = if (bestSet != null) {
                                        BodyMath.calculateEpley1RM(bestSet.weight ?: 0.0, bestSet.reps ?: 0)
                                    } else maxWeight
                                    val bestReps = bestSet?.reps ?: 0

                                    if (maxWeight > 0.0 || best1RM > 0.0) {
                                        ExerciseSessionPoint(
                                            dateMillis = record.date,
                                            maxWeight = maxWeight,
                                            estimated1RM = best1RM,
                                            bestReps = bestReps
                                        )
                                    } else null
                                }
                            }

                            ExerciseProgressChart(
                                points = chartPoints,
                                unit = preferredUnit,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Recent Records Header
                        item {
                            Text(
                                text = "Recent Records",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            )
                        }

                        // Real Recent Records List from Room DB
                        if (state.history.isEmpty()) {
                            item {
                                GymCard(backgroundColor = PrimaryCard) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No recorded sessions yet. Log this exercise in a routine to begin tracking progress.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText, textAlign = TextAlign.Center)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(state.history) { record ->
                                val dateStr = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(record.date))
                                val maxReps = record.sets.maxOfOrNull { it.reps ?: 0 } ?: 0
                                GymCard(backgroundColor = PrimaryCard) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = dateStr,
                                                style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                                            )
                                            Text(
                                                text = "${record.sets.size} sets logged",
                                                style = MaterialTheme.typography.labelSmall.copy(color = MutedText)
                                            )
                                        }
                                        Text(
                                            text = "${record.maxWeight.roundToInt()} $preferredUnit × $maxReps reps",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryText
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                "About" -> {
                    // TAB 3: ABOUT / INSTRUCTIONS
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            GymCard(backgroundColor = PrimaryCard) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "EXECUTION INSTRUCTIONS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = PrimaryPurple,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = exercise.instructions ?: "Grip the barbell securely. Maintain core engagement and execute with controlled eccentric cadence.",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = PrimaryText, lineHeight = 22.sp)
                                    )
                                }
                            }
                        }

                        item {
                            GymCard(backgroundColor = PrimaryCard) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "TARGET MUSCLES",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MutedText,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Primary: ${exercise.primaryMuscleGroup}\nSecondary: ${exercise.secondaryMuscleGroups.ifEmpty { "Triceps, Shoulders" }}",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    ConfirmationDialog(
        show = showArchiveDialog,
        title = "Archive Exercise?",
        message = "Hide this exercise from active library? Historical records remain preserved.",
        confirmText = "Archive",
        isDestructive = true,
        onConfirm = {
            viewModel.archiveExercise(exerciseId) { onNavigateBack() }
        },
        onDismiss = { showArchiveDialog = false }
    )
}
