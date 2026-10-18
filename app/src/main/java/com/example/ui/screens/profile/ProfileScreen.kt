package com.example.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.GymAvatar
import com.example.ui.components.GymCard
import com.example.ui.components.GymGradientButton
import com.example.ui.components.GymSecondaryButton
import com.example.ui.components.GymTextField
import com.example.ui.components.StatCard
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToBackupRestore: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = state.profile

    var showPhotoActionsDialog by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditHeightDialog by remember { mutableStateOf(false) }
    var showEditGoalDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setProfilePicture(uri)
        }
    }

    val dobDateStr = remember(profile.dateOfBirth) {
        if (profile.dateOfBirth != null) {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            sdf.format(Date(profile.dateOfBirth))
        } else {
            "Not set"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Profile Picture and Name Hero Section
        item {
            GymCard(
                backgroundColor = PrimaryCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        GymAvatar(
                            imagePath = profile.profilePicturePath,
                            size = 96.dp,
                            onClick = { showPhotoActionsDialog = true }
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple)
                                .clickable { showPhotoActionsDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Change profile picture",
                                tint = PrimaryText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showEditNameDialog = true }
                    ) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit name",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = profile.fitnessGoal ?: "Set your fitness goals",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText)
                    )
                }
            }
        }

        // 2. Personal Metrics (Age, Height, Preferred Unit)
        item {
            Text(
                text = "PERSONAL INFORMATION",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MutedText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // DOB & Age
                GymCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePickerDialog = true },
                    backgroundColor = PrimaryCard
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AGE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MutedText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (state.calculatedAge != null) "${state.calculatedAge} yrs" else "--",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dobDateStr,
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText),
                            maxLines = 1
                        )
                    }
                }

                // Height
                GymCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEditHeightDialog = true },
                    backgroundColor = PrimaryCard
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "HEIGHT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MutedText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.Height,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (profile.heightCm != null) "${profile.heightCm?.roundToInt()} cm" else "--",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to update",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                        )
                    }
                }
            }
        }

        // 3. Preferred Weight Unit Switcher (kg vs lb)
        item {
            GymCard(backgroundColor = PrimaryCard) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Preferred Weight Unit",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        )
                        Text(
                            text = "Used across all exercises, sets, and progress charts",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SecondaryCard)
                            .padding(3.dp)
                    ) {
                        val isKg = profile.preferredWeightUnit.equals("kg", ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isKg) PrimaryPurple else ElevatedSurface)
                                .clickable { viewModel.updatePreferredWeightUnit("kg") }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "kg",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isKg) PrimaryText else SecondaryText
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isKg) PrimaryPurple else ElevatedSurface)
                                .clickable { viewModel.updatePreferredWeightUnit("lb") }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "lb",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isKg) PrimaryText else SecondaryText
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. Fitness Goals & Notes
        item {
            GymCard(
                backgroundColor = PrimaryCard,
                modifier = Modifier.clickable { showEditGoalDialog = true }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FITNESS GOALS & TRAINING FOCUS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Icon(Icons.Default.Edit, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile.fitnessGoal ?: "Tap to add your fitness goal",
                        style = MaterialTheme.typography.bodyMedium.copy(color = PrimaryText)
                    )
                    if (!profile.personalNotes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = profile.personalNotes ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                        )
                    }
                }
            }
        }

        // 5. Backup & Privacy Navigation
        item {
            Text(
                text = "DATA & PRIVACY",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MutedText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }

        item {
            GymSecondaryButton(
                text = "Backup, Export & Restore",
                icon = Icons.Default.Backup,
                onClick = onNavigateToBackupRestore,
                modifier = Modifier.fillMaxWidth(),
                testTag = "open_backup_restore_btn"
            )
        }

        // 6. Privacy & Offline Banner
        item {
            GymCard(backgroundColor = SecondaryCard) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "100% Offline & Private. Your profile photos, workout history, and personal logs stay exclusively on your device.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                    )
                }
            }
        }
    }

    // Photo Actions Dialog (Select, Replace, Remove)
    if (showPhotoActionsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoActionsDialog = false },
            title = {
                Text("Profile Photo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryText))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GymSecondaryButton(
                        text = if (profile.profilePicturePath.isNullOrBlank()) "Select Photo" else "Replace Photo",
                        icon = Icons.Default.AddAPhoto,
                        onClick = {
                            showPhotoActionsDialog = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!profile.profilePicturePath.isNullOrBlank()) {
                        GymSecondaryButton(
                            text = "Remove Photo",
                            icon = Icons.Default.Delete,
                            onClick = {
                                viewModel.removeProfilePicture()
                                showPhotoActionsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoActionsDialog = false }) {
                    Text("Close", color = SecondaryText)
                }
            },
            containerColor = PrimaryCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Date Picker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = profile.dateOfBirth ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.updateDateOfBirth(it)
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Confirm", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel", color = SecondaryText)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        var nameInput by remember { mutableStateOf(profile.name) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Name", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryText)) },
            text = {
                GymTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = "Your Name",
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                GymGradientButton(
                    text = "Save",
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            viewModel.updateName(nameInput.trim())
                        }
                        showEditNameDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = SecondaryText)
                }
            },
            containerColor = PrimaryCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Edit Height Dialog
    if (showEditHeightDialog) {
        var heightInput by remember { mutableStateOf(profile.heightCm?.roundToInt()?.toString() ?: "175") }
        AlertDialog(
            onDismissRequest = { showEditHeightDialog = false },
            title = { Text("Update Height", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryText)) },
            text = {
                GymTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    label = "Height (cm)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                GymGradientButton(
                    text = "Save",
                    onClick = {
                        val parsed = heightInput.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            viewModel.updateHeight(parsed)
                        }
                        showEditHeightDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showEditHeightDialog = false }) {
                    Text("Cancel", color = SecondaryText)
                }
            },
            containerColor = PrimaryCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Edit Goal Dialog
    if (showEditGoalDialog) {
        var goalInput by remember { mutableStateOf(profile.fitnessGoal ?: "") }
        var notesInput by remember { mutableStateOf(profile.personalNotes ?: "") }
        AlertDialog(
            onDismissRequest = { showEditGoalDialog = false },
            title = { Text("Fitness Goals & Notes", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryText)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GymTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it },
                        label = "Primary Goal",
                        placeholder = "e.g. Strength & Hypertrophy",
                        modifier = Modifier.fillMaxWidth()
                    )
                    GymTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = "Personal Notes",
                        placeholder = "Training frequency, injury notes, preferences...",
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                GymGradientButton(
                    text = "Save",
                    onClick = {
                        viewModel.updateFitnessGoal(goalInput.trim())
                        viewModel.updateNotes(notesInput.trim())
                        showEditGoalDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showEditGoalDialog = false }) {
                    Text("Cancel", color = SecondaryText)
                }
            },
            containerColor = PrimaryCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
