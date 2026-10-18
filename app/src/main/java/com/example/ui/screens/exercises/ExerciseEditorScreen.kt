package com.example.ui.screens.exercises

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.ExerciseType
import com.example.data.model.LoadEntryMode
import com.example.data.model.MuscleGroup
import com.example.ui.components.ExerciseImage
import com.example.ui.components.GymCard
import com.example.ui.components.GymGradientButton
import com.example.ui.components.GymSecondaryButton
import com.example.ui.components.GymTextField
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
fun ExerciseEditorScreen(
    exerciseId: String?,
    viewModel: ExercisesViewModel,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(exerciseId) {
        viewModel.prepareEditor(exerciseId)
    }

    val state by viewModel.editorState.collectAsStateWithLifecycle()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onEditorImagePicked(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (exerciseId == null) "New Exercise" else "Edit Exercise")
                },
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
                .testTag("exercise_editor_screen"),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Large Image Preview and Photo Picker Section
            item {
                GymCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PrimaryCard
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val hasImage = state.pendingImageUri != null || (!state.currentImagePath.isNullOrBlank() && !state.isImageRemoved)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElevatedSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.pendingImageUri != null) {
                                AsyncImage(
                                    model = state.pendingImageUri,
                                    contentDescription = "Selected exercise image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (!state.currentImagePath.isNullOrBlank() && !state.isImageRemoved) {
                                ExerciseImage(
                                    imagePath = state.currentImagePath,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Add Exercise Photo",
                                        style = MaterialTheme.typography.labelMedium.copy(color = SecondaryText)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!hasImage) {
                                GymGradientButton(
                                    text = "Choose Image",
                                    icon = Icons.Default.AddPhotoAlternate,
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "choose_exercise_image_btn"
                                )
                            } else {
                                GymSecondaryButton(
                                    text = "Change",
                                    icon = Icons.Default.Edit,
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    testTag = "change_exercise_image_btn"
                                )
                                GymSecondaryButton(
                                    text = "Remove",
                                    icon = Icons.Default.Delete,
                                    onClick = { viewModel.onEditorImageRemoved() },
                                    modifier = Modifier.weight(1f),
                                    testTag = "remove_exercise_image_btn"
                                )
                            }
                        }
                    }
                }
            }

            // 2. Exercise Form Fields
            item {
                GymTextField(
                    value = state.name,
                    onValueChange = { viewModel.onEditorNameChanged(it) },
                    label = "Exercise Name *",
                    placeholder = "e.g. Incline Dumbbell Press",
                    isError = state.error != null,
                    errorMessage = state.error,
                    modifier = Modifier.fillMaxWidth().testTag("exercise_name_input")
                )
            }

            // 3. Primary Muscle Group
            item {
                Column {
                    Text(
                        text = "Primary Muscle Group",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = SecondaryText,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(MuscleGroup.entries.toList()) { group ->
                            val isSelected = state.primaryMuscleGroup == group.name
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) PrimaryPurple else SecondaryCard,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, DividerDark),
                                modifier = Modifier.clickable {
                                    viewModel.onEditorMuscleGroupChanged(group.name)
                                }
                            ) {
                                Text(
                                    text = group.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) PrimaryText else SecondaryText,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Equipment
            item {
                GymTextField(
                    value = state.equipment,
                    onValueChange = { viewModel.onEditorEquipmentChanged(it) },
                    label = "Equipment",
                    placeholder = "e.g. Barbell, Dumbbells, Machine, Cable, None",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 5. Instructions
            item {
                GymTextField(
                    value = state.instructions,
                    onValueChange = { viewModel.onEditorInstructionsChanged(it) },
                    label = "Instructions (Optional)",
                    placeholder = "Step-by-step technique guidance...",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 6. Notes
            item {
                GymTextField(
                    value = state.notes,
                    onValueChange = { viewModel.onEditorNotesChanged(it) },
                    label = "Personal Notes (Optional)",
                    placeholder = "Grip cue, seat position, pin setting...",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 7. Save and Cancel Buttons
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GymGradientButton(
                        text = "Save Exercise",
                        onClick = {
                            viewModel.saveExercise {
                                onNavigateBack()
                            }
                        },
                        isLoading = state.isSaving,
                        modifier = Modifier.weight(1f),
                        testTag = "save_exercise_btn"
                    )
                    GymSecondaryButton(
                        text = "Cancel",
                        onClick = onNavigateBack,
                        testTag = "cancel_exercise_btn"
                    )
                }
            }
        }
    }
}
