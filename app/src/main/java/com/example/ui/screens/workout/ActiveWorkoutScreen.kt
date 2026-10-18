package com.example.ui.screens.workout

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SessionExerciseWithSets
import com.example.data.model.WorkoutSet
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.ExerciseImage
import com.example.ui.components.GymCard
import com.example.ui.components.GymGradientButton
import com.example.ui.components.GymSecondaryButton
import com.example.ui.screens.plan.AddExerciseSelectionDialog
import com.example.ui.theme.AccentError
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.DividerDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryCard
import com.example.ui.theme.SecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    viewModel: ActiveWorkoutViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sessionWithDetails = state.sessionWithDetails

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var activeExerciseIndex by remember { mutableStateOf(1) } // default active exercise index 1 (0-based)

    BackHandler {
        onNavigateBack()
    }

    val minutes = state.elapsedDurationSeconds / 60
    val seconds = state.elapsedDurationSeconds % 60
    val timerFormatted = String.format("%02d:%02d:%02d", minutes / 60, minutes % 60, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = sessionWithDetails?.session?.name ?: "Push Day",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
                    }
                },
                actions = {
                    // Live green/cyan timer matching Image 1 Screen 8
                    Text(
                        text = timerFormatted,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald,
                            fontSize = 15.sp
                        ),
                        modifier = Modifier.padding(end = 6.dp)
                    )

                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = SecondaryText)
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(PrimaryCard)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add Exercise", color = PrimaryText) },
                            onClick = {
                                menuExpanded = false
                                showAddExerciseDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Discard Workout", color = AccentError) },
                            onClick = {
                                menuExpanded = false
                                showDiscardDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = PrimaryText,
                    navigationIconContentColor = PrimaryText
                )
            )
        },
        bottomBar = {
            // Bottom Action Bar matching Image 1: [End Workout] [Next Exercise]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDark)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GymSecondaryButton(
                    text = "End Workout",
                    onClick = { viewModel.finishWorkout(onNavigateBack) },
                    modifier = Modifier.weight(1f)
                )

                GymGradientButton(
                    text = if (sessionWithDetails != null && activeExerciseIndex >= sessionWithDetails.exercises.size - 1) "Finish Workout" else "Next Exercise",
                    onClick = {
                        if (sessionWithDetails != null && activeExerciseIndex < sessionWithDetails.exercises.size - 1) {
                            activeExerciseIndex++
                        } else {
                            viewModel.finishWorkout(onNavigateBack)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        if (sessionWithDetails == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No active workout session.", color = SecondaryText)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundDark)
                .testTag("active_workout_screen"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Numbered Exercise Cards matching Image 1 Screen 8
            itemsIndexed(sessionWithDetails.exercises) { index, sessionExercise ->
                val isSelected = activeExerciseIndex == index
                val isCompleted = sessionExercise.completedSetsCount > 0 && sessionExercise.completedSetsCount >= sessionExercise.sets.size

                ActiveExerciseCardImage1(
                    index = index + 1,
                    sessionExercise = sessionExercise,
                    isSelected = isSelected,
                    isCompleted = isCompleted,
                    onClick = { activeExerciseIndex = index },
                    onAddSet = { viewModel.addSet(sessionExercise.exercise.id) },
                    onCompleteSet = { setId, done -> viewModel.completeSet(setId, done) },
                    onUpdateSet = { set, w, r -> viewModel.updateSetValues(set, w, r) }
                )
            }

            // Quick Add Exercise Button
            item {
                Spacer(modifier = Modifier.height(6.dp))
                GymSecondaryButton(
                    text = "+ Add Another Exercise",
                    onClick = { showAddExerciseDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showAddExerciseDialog) {
        AddExerciseSelectionDialog(
            exercises = state.availableExercises,
            onSelect = { ex ->
                viewModel.addExerciseToWorkout(ex.id)
                showAddExerciseDialog = false
            },
            onDismiss = { showAddExerciseDialog = false }
        )
    }

    ConfirmationDialog(
        show = showDiscardDialog,
        title = "Discard Workout?",
        message = "Are you sure you want to discard this workout? Current session data will be cleared.",
        confirmText = "Discard",
        isDestructive = true,
        onConfirm = {
            viewModel.discardWorkout(onNavigateBack)
            showDiscardDialog = false
        },
        onDismiss = { showDiscardDialog = false }
    )

    if (state.showSummaryDialog && state.completedSummary != null) {
        WorkoutSummaryDialog(
            session = state.completedSummary!!,
            onDismiss = { viewModel.dismissSummary(onNavigateBack) }
        )
    }
}

@Composable
fun ActiveExerciseCardImage1(
    index: Int,
    sessionExercise: SessionExerciseWithSets,
    isSelected: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
    onAddSet: () -> Unit,
    onCompleteSet: (String, Boolean) -> Unit,
    onUpdateSet: (WorkoutSet, Double?, Int?) -> Unit
) {
    val se = sessionExercise.exercise

    GymCard(
        backgroundColor = PrimaryCard,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exercise Index Number
                Text(
                    text = "$index",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SecondaryText
                    ),
                    modifier = Modifier.width(22.dp)
                )

                // Exercise Image Thumbnail
                ExerciseImage(
                    imagePath = se.imagePath,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = se.exerciseName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${sessionExercise.sets.size} sets • 8-12 reps",
                        style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                    )
                }

                // Action / Status Badge matching Image 1 Screen 8:
                // Completed -> Cyan checkmark circle
                // Active -> Purple play circle
                // Upcoming -> Right chevron
                when {
                    isCompleted -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NeonCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                    }
                    isSelected -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PrimaryText, modifier = Modifier.size(18.dp))
                        }
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Expandable Sets Table for active/selected exercise
            AnimatedVisibility(visible = isSelected) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SET", style = MaterialTheme.typography.labelSmall.copy(color = MutedText, fontWeight = FontWeight.Bold), modifier = Modifier.width(36.dp))
                        Spacer(modifier = Modifier.weight(1f))
                        Text("KG", style = MaterialTheme.typography.labelSmall.copy(color = MutedText, fontWeight = FontWeight.Bold), modifier = Modifier.width(80.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("REPS", style = MaterialTheme.typography.labelSmall.copy(color = MutedText, fontWeight = FontWeight.Bold), modifier = Modifier.width(80.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DONE", style = MaterialTheme.typography.labelSmall.copy(color = MutedText, fontWeight = FontWeight.Bold), modifier = Modifier.width(40.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    sessionExercise.sets.forEach { set ->
                        var weightStr by remember(set.weight) { mutableStateOf(set.weight?.toString() ?: "") }
                        var repsStr by remember(set.reps) { mutableStateOf(set.reps?.toString() ?: "") }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${set.setNumber}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryText),
                                modifier = Modifier.width(36.dp)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            OutlinedTextField(
                                value = weightStr,
                                onValueChange = {
                                    weightStr = it
                                    onUpdateSet(set, it.toDoubleOrNull(), repsStr.toIntOrNull())
                                },
                                placeholder = { Text("0", color = MutedText) },
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
                                modifier = Modifier.width(80.dp).height(44.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedTextField(
                                value = repsStr,
                                onValueChange = {
                                    repsStr = it
                                    onUpdateSet(set, weightStr.toDoubleOrNull(), it.toIntOrNull())
                                },
                                placeholder = { Text("0", color = MutedText) },
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
                                modifier = Modifier.width(80.dp).height(44.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (set.isCompleted) NeonEmerald else ElevatedSurface)
                                    .clickable { onCompleteSet(set.id, !set.isCompleted) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (set.isCompleted) Color.Black else SecondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "+ Add Set",
                        style = MaterialTheme.typography.labelMedium.copy(color = PrimaryPurple, fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .clickable(onClick = onAddSet)
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
