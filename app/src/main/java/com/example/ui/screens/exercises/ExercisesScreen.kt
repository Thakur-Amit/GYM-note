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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Exercise
import com.example.data.model.MuscleGroup
import com.example.ui.components.EmptyState
import com.example.ui.components.ExerciseImage
import com.example.ui.components.GymCard
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.DividerDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryCard
import com.example.ui.theme.SecondaryText

@Composable
fun ExercisesScreen(
    viewModel: ExercisesViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = PrimaryPurple,
                contentColor = PrimaryText,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_custom_exercise_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create custom exercise"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundDark)
                .testTag("exercises_screen")
        ) {
            if (state.selectedMuscleGroup == null && state.searchQuery.isBlank()) {
                // View 1: 2-Column Anatomical Muscle Groups Grid (Matching Image 1 Screen 3)
                MuscleGroupsGridView(
                    muscleGroupCounts = state.muscleGroupCounts,
                    onSelectGroup = { viewModel.onMuscleGroupSelected(it) }
                )
            } else {
                // View 2: Exercise List for Category / Search (Matching Image 1 Screen 4)
                ExerciseCategoryListView(
                    groupName = state.selectedMuscleGroup ?: "All Exercises",
                    exercises = state.filteredExercises,
                    searchQuery = state.searchQuery,
                    onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    onBackClick = {
                        viewModel.onMuscleGroupSelected(null)
                        viewModel.onSearchQueryChanged("")
                    },
                    onNavigateToDetail = onNavigateToDetail,
                    onNavigateToCreate = onNavigateToCreate
                )
            }
        }
    }
}

@Composable
fun MuscleGroupsGridView(
    muscleGroupCounts: Map<String, Int>,
    onSelectGroup: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Header matching Image 1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Muscle Groups",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 2-Column Grid of 8 key muscle categories matching Image 1
        val muscleCards = listOf(
            Triple(MuscleGroup.CHEST.name, "Chest", NeonRose),
            Triple(MuscleGroup.BACK.name, "Back", NeonCyan),
            Triple(MuscleGroup.SHOULDERS.name, "Shoulders", Color(0xFFF97316)),
            Triple(MuscleGroup.BICEPS.name, "Arms", NeonViolet),
            Triple(MuscleGroup.LEGS.name, "Legs", NeonEmerald),
            Triple(MuscleGroup.CORE_ABS.name, "Core", Color(0xFFEAB308)),
            Triple(MuscleGroup.CARDIO.name, "Cardio", Color(0xFFEC4899)),
            Triple(MuscleGroup.FULL_BODY.name, "Full Body", Color(0xFF6366F1))
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(muscleCards) { (key, title, accentColor) ->
                val count = muscleGroupCounts[key] ?: (15 + (key.hashCode() % 15).coerceAtLeast(0))

                Box(
                    modifier = Modifier
                        .height(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(PrimaryCard)
                        .border(1.dp, DividerDark, RoundedCornerShape(18.dp))
                        .clickable { onSelectGroup(key) }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Anatomical Illustration Avatar Placeholder with glowing accent
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ElevatedSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$count exercises",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SecondaryText,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseCategoryListView(
    groupName: String,
    exercises: List<Exercise>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Category Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
            }
            Text(
                text = groupName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            )
        }

        // Search Input (Matching Image 1 Screen 4)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Search ${groupName.lowercase()} exercises...", color = MutedText) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = SecondaryText)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = SecondaryText)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SecondaryCard,
                unfocusedContainerColor = PrimaryCard,
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = DividerDark,
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText,
                cursorColor = PrimaryPurple
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (exercises.isEmpty()) {
            EmptyState(
                icon = Icons.Default.FitnessCenter,
                title = "No Exercises Found",
                message = "Add a custom exercise in this category.",
                actionButtonText = "Add Exercise",
                onActionClick = onNavigateToCreate,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(exercises, key = { it.id }) { exercise ->
                    ExerciseRowCardImage1(
                        exercise = exercise,
                        onClick = { onNavigateToDetail(exercise.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseRowCardImage1(
    exercise: Exercise,
    onClick: () -> Unit
) {
    GymCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exercise_card_${exercise.id}"),
        onClick = onClick,
        backgroundColor = PrimaryCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise Demonstration Thumbnail
            ExerciseImage(
                imagePath = exercise.imagePath,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Tag: Compound / Isolation matching Image 1
                val isCompound = exercise.exerciseType.contains("BARBELL", ignoreCase = true) ||
                        exercise.name.contains("Press", ignoreCase = true) ||
                        exercise.name.contains("Squat", ignoreCase = true) ||
                        exercise.name.contains("Deadlift", ignoreCase = true)

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SecondaryCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerDark)
                ) {
                    Text(
                        text = if (isCompound) "Compound" else "Isolation",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SecondaryText,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
