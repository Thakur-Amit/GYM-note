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
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.ExerciseImage
import com.example.ui.components.GymCard
import com.example.ui.components.GymGradientButton
import com.example.ui.components.GymSecondaryButton
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.DividerDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.NeonAmber
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
    var weight: String,
    var reps: String,
    var isDone: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    viewModel: ExercisesViewModel,
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
    var selectedTab by remember { mutableStateOf(0) } // 0 = Log, 1 = History, 2 = About
    var selectedHistoryRange by remember { mutableStateOf("1M") }

    // Quick set rows matching Image 1
    val setsList = remember {
        mutableStateListOf(
            QuickSetRowState(1, "80", "8", true),
            QuickSetRowState(2, "80", "8", true),
            QuickSetRowState(3, "75", "10", true),
            QuickSetRowState(4, "75", "10", true)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Exercise Details", maxLines = 1, fontWeight = FontWeight.Bold) },
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
            // Segmented Pill Tabs matching Image 1: [Log] [History] [About]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryCard)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val tabs = listOf("Log", "History", "About")
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) PrimaryPurple else Color.Transparent)
                            .clickable { selectedTab = index },
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

            when (selectedTab) {
                0 -> {
                    // TAB 1: LOG SCREEN (Matching Image 1 Screen 5)
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

                        // Previous Record Card matching Image 1
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
                                    Column {
                                        Text(
                                            text = "Previous Record",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MutedText)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val prWeight = state.prSummary.maxWeight?.roundToInt() ?: 80
                                        Text(
                                            text = "$prWeight kg × 8 reps",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryText
                                            )
                                        )
                                    }

                                    Text(
                                        text = "12 Oct 2026",
                                        style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                                    )
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

                        // Set Rows matching Image 1
                        itemsIndexed(setsList) { index, setItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryCard)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Set ${setItem.setNumber}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryText
                                    ),
                                    modifier = Modifier.width(60.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                // Weight box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ElevatedSurface)
                                        .border(1.dp, DividerDark, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = setItem.weight,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryText
                                        )
                                    )
                                }

                                Text(
                                    text = "×",
                                    style = MaterialTheme.typography.titleMedium.copy(color = MutedText),
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )

                                // Reps box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ElevatedSurface)
                                        .border(1.dp, DividerDark, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = setItem.reps,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryText
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Green Checkmark circle button
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
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Add Set Button
                        item {
                            GymSecondaryButton(
                                text = "Add Set +",
                                onClick = {
                                    val last = setsList.lastOrNull()
                                    setsList.add(
                                        QuickSetRowState(
                                            setNumber = setsList.size + 1,
                                            weight = last?.weight ?: "80",
                                            reps = last?.reps ?: "8",
                                            isDone = true
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Save Workout Button (Purple gradient)
                        item {
                            GymGradientButton(
                                text = "Save Workout",
                                onClick = onNavigateBack,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                1 -> {
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

                        // Dual Bar Chart Card (Weight vs Reps)
                        item {
                            GymCard(backgroundColor = PrimaryCard) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Legend
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(PrimaryPurple))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Weight (kg)", style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText))
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(NeonAmber))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Reps", style = MaterialTheme.typography.labelSmall.copy(color = SecondaryText))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Bar chart visualization
                                    val historyBars = listOf(
                                        Triple("Sep 15", 70, 8),
                                        Triple("Sep 22", 75, 10),
                                        Triple("Sep 29", 75, 8),
                                        Triple("Oct 6", 80, 8),
                                        Triple("Oct 13", 85, 8)
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        historyBars.forEach { (date, w, r) ->
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                    verticalAlignment = Alignment.Bottom
                                                ) {
                                                    // Weight bar (Purple)
                                                    Box(
                                                        modifier = Modifier
                                                            .width(10.dp)
                                                            .height(((w / 100f) * 90).dp)
                                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                            .background(PrimaryPurple)
                                                    )
                                                    // Reps bar (Orange)
                                                    Box(
                                                        modifier = Modifier
                                                            .width(10.dp)
                                                            .height(((r / 15f) * 70).dp)
                                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                            .background(NeonAmber)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(date, style = MaterialTheme.typography.labelSmall.copy(color = MutedText, fontSize = 9.sp))
                                            }
                                        }
                                    }
                                }
                            }
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

                        // Recent Records List matching Image 1
                        val recentItems = listOf(
                            "12 Oct 2026" to "80 kg × 8 reps",
                            "8 Oct 2026" to "75 kg × 10 reps",
                            "5 Oct 2026" to "70 kg × 8 reps",
                            "1 Oct 2026" to "70 kg × 8 reps",
                            "28 Sep 2026" to "65 kg × 10 reps"
                        )

                        items(recentItems) { (recordDate, performance) ->
                            GymCard(backgroundColor = PrimaryCard) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = recordDate,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                                    )
                                    Text(
                                        text = performance,
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
                2 -> {
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
