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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Exercise
import com.example.ui.components.EmptyState
import com.example.ui.components.ExerciseImage
import com.example.ui.components.GymCard
import com.example.ui.components.GymGradientButton
import com.example.ui.components.GymSecondaryButton
import com.example.ui.components.GymTextField
import com.example.ui.components.MuscleGroupBadge
import com.example.ui.theme.AccentError
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.DividerDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryCard
import com.example.ui.theme.SecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditorScreen(
    templateId: String?,
    viewModel: PlanViewModel,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(templateId) {
        viewModel.prepareTemplateEditor(templateId)
    }

    val state by viewModel.editorState.collectAsStateWithLifecycle()
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (templateId == null) "Create Template" else "Edit Template") },
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
                .testTag("template_editor_screen"),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Template Info Form
            item {
                GymTextField(
                    value = state.name,
                    onValueChange = { viewModel.onEditorNameChanged(it) },
                    label = "Template Name *",
                    placeholder = "e.g. Upper Body Power",
                    isError = state.error != null,
                    errorMessage = state.error,
                    modifier = Modifier.fillMaxWidth().testTag("template_name_input")
                )
            }

            item {
                GymTextField(
                    value = state.description,
                    onValueChange = { viewModel.onEditorDescriptionChanged(it) },
                    label = "Description (Optional)",
                    placeholder = "e.g. Focus on chest press and lat pulldowns",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Exercise List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXERCISES (${state.exercises.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    GymSecondaryButton(
                        text = "Add Exercise",
                        icon = Icons.Default.Add,
                        onClick = { showAddExerciseDialog = true },
                        testTag = "add_exercise_to_template_btn"
                    )
                }
            }

            // Exercises
            if (state.exercises.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.FitnessCenter,
                        title = "No Exercises Added",
                        message = "Add exercises to build your workout template.",
                        actionButtonText = "Add First Exercise",
                        onActionClick = { showAddExerciseDialog = true }
                    )
                }
            } else {
                itemsIndexed(state.exercises) { index, item ->
                    GymCard(backgroundColor = PrimaryCard, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ExerciseImage(
                                imagePath = item.imagePath,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.exerciseName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    MuscleGroupBadge(muscleGroup = item.primaryMuscleGroup)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${item.targetSets} sets • ${item.targetRepRange} reps",
                                        style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.removeExerciseFromTemplate(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = AccentError)
                            }
                        }
                    }
                }
            }

            // Save and Cancel
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GymGradientButton(
                        text = "Save Template",
                        onClick = {
                            viewModel.saveTemplate {
                                onNavigateBack()
                            }
                        },
                        isLoading = state.isSaving,
                        modifier = Modifier.weight(1f),
                        testTag = "save_template_btn"
                    )
                    GymSecondaryButton(
                        text = "Cancel",
                        onClick = onNavigateBack,
                        testTag = "cancel_template_btn"
                    )
                }
            }
        }
    }

    if (showAddExerciseDialog) {
        AddExerciseSelectionDialog(
            exercises = state.availableExercises,
            onSelect = { exercise ->
                viewModel.addExerciseToTemplate(exercise)
                showAddExerciseDialog = false
            },
            onDismiss = { showAddExerciseDialog = false }
        )
    }
}

@Composable
fun AddExerciseSelectionDialog(
    exercises: List<Exercise>,
    onSelect: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, exercises) {
        if (searchQuery.isBlank()) exercises
        else exercises.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.primaryMuscleGroup.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Exercise",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search exercises...", color = MutedText) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SecondaryCard,
                        unfocusedContainerColor = PrimaryCard,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DividerDark,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered) { ex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SecondaryCard)
                                .clickable { onSelect(ex) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ExerciseImage(
                                imagePath = ex.imagePath,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ex.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    )
                                )
                                Text(
                                    text = ex.primaryMuscleGroup,
                                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SecondaryText)
            }
        },
        containerColor = PrimaryCard,
        shape = RoundedCornerShape(16.dp)
    )
}
