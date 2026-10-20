package com.example.ui.screens.plan

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.model.WorkoutTemplateWithExercises
import com.example.data.repository.GymRepository
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.ExerciseImage
import com.example.ui.components.GymCard
import com.example.ui.components.GymGradientButton
import com.example.ui.components.GymSecondaryButton
import com.example.ui.theme.AccentError
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryCard
import com.example.ui.theme.SecondaryText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    templateId: String,
    repository: GymRepository,
    onNavigateBack: () -> Unit,
    onNavigateToExerciseDetail: (exerciseId: String, fromWorkout: Boolean, routineName: String) -> Unit,
    onStartWorkout: (templateId: String) -> Unit,
    onEditTemplate: (templateId: String) -> Unit
) {
    val templateWithExercises by repository.getTemplateWithExercises(templateId)
        .collectAsStateWithLifecycle(initialValue = null)
    val availableExercises by repository.activeExercises
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val coroutineScope = rememberCoroutineScope()
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var exerciseToDeleteId by remember { mutableStateOf<Long?>(null) }

    val template = templateWithExercises?.template
    val exercises = templateWithExercises?.exercises ?: emptyList()
    val completedExerciseIdsToday by repository.getCompletedExerciseIdsToday()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val (completedList, uncompletedList) = exercises.partition { detail ->
        detail.exercise?.id in completedExerciseIdsToday
    }
    val sortedExercises = uncompletedList + completedList

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = template?.dayOfWeek?.uppercase() ?: "ROUTINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryPurple,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = template?.name ?: "Workout Routine",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            ),
                            maxLines = 1
                        )
                    }
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
                    IconButton(onClick = { onEditTemplate(templateId) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Routine",
                            tint = SecondaryText
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
        containerColor = BackgroundDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundDark)
                .testTag("routine_detail_screen"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info Card
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
                                text = "${exercises.size} Exercises Planned",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ElevatedSurface)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = template?.dayOfWeek ?: "Scheduled",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = NeonEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        if (!template?.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = template?.description ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                            )
                        }
                    }
                }
            }

            // Prominent "+ Add Exercise" Button (as requested by user)
            item {
                GymSecondaryButton(
                    text = "+ Add Exercise to Routine",
                    icon = Icons.Default.Add,
                    onClick = { showAddExerciseDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_exercise_to_routine_btn")
                )
            }

            // Exercise List
            if (exercises.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.FitnessCenter,
                        title = "No Exercises in this Routine",
                        message = "Tap '+ Add Exercise to Routine' above to add your first movement.",
                        actionButtonText = "Add Exercise",
                        onActionClick = { showAddExerciseDialog = true }
                    )
                }
            } else {
                itemsIndexed(sortedExercises) { index, detail ->
                    val ex = detail.exercise
                    val te = detail.templateExercise
                    val isCompleted = ex?.id in completedExerciseIdsToday

                    GymCard(
                        backgroundColor = PrimaryCard,
                        borderColor = if (isCompleted) Color(0xFF10B981) else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (ex != null) {
                                    onNavigateToExerciseDetail(
                                        ex.id,
                                        true,
                                        template?.name ?: "Workout"
                                    )
                                }
                            }
                            .testTag("routine_exercise_item_${te.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompleted) Color(0xFF10B981) else SecondaryText
                                ),
                                modifier = Modifier.width(24.dp)
                            )

                            ExerciseImage(
                                imagePath = ex?.imagePath,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = ex?.name ?: "Exercise",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryText
                                        ),
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (isCompleted) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Finished",
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Done",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color(0xFF10B981),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "${te.targetSets} sets • ${te.targetRepRange} reps",
                                        style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                                    )
                                    if (te.targetWeight != null && te.targetWeight > 0.0) {
                                        Text(
                                            text = "• ${te.targetWeight.toInt()} kg",
                                            style = MaterialTheme.typography.bodySmall.copy(color = NeonCyan)
                                        )
                                    }
                                }
                            }

                            // Delete button to remove exercise from routine
                            IconButton(
                                onClick = { exerciseToDeleteId = te.id },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove exercise",
                                    tint = AccentError.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open Exercise",
                                tint = SecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddExerciseDialog) {
        AddExerciseSelectionDialog(
            exercises = availableExercises,
            onSelect = { selectedExercise ->
                coroutineScope.launch {
                    repository.addExerciseToTemplate(templateId, selectedExercise.id)
                }
                showAddExerciseDialog = false
            },
            onDismiss = { showAddExerciseDialog = false }
        )
    }

    exerciseToDeleteId?.let { teId ->
        ConfirmationDialog(
            show = true,
            title = "Remove Exercise?",
            message = "Remove this exercise from '${template?.name ?: "this routine"}'?",
            confirmText = "Remove",
            isDestructive = true,
            onConfirm = {
                coroutineScope.launch {
                    repository.removeExerciseFromTemplate(teId)
                }
                exerciseToDeleteId = null
            },
            onDismiss = { exerciseToDeleteId = null }
        )
    }
}
