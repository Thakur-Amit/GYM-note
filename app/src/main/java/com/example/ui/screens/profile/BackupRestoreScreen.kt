package com.example.ui.screens.profile

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.GymCard
import com.example.ui.components.GymGradientButton
import com.example.ui.components.GymSecondaryButton
import com.example.ui.theme.AccentError
import com.example.ui.theme.AccentSuccess
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
fun BackupRestoreScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreInputJson by remember { mutableStateOf("") }
    var showConfirmRestoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Data Export") },
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
                .testTag("backup_restore_screen"),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Privacy Guarantee Card
            item {
                GymCard(backgroundColor = PrimaryCard) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AccentSuccess,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Zero Cloud Tracking",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "All fitness journals, weights, and photos are stored on-device. Export backups regularly to ensure you can restore them on a new phone.",
                                style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                            )
                        }
                    }
                }
            }

            // Export Section
            item {
                Text(
                    text = "EXPORT BACKUP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MutedText,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            // JSON Backup Export
            item {
                GymCard(backgroundColor = PrimaryCard) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Full Database Backup (JSON)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Exports complete profile, exercise library, templates, and full workout history.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        GymGradientButton(
                            text = if (state.exportJsonString == null) "Generate JSON Backup" else "Copy JSON to Clipboard",
                            icon = Icons.Default.FileUpload,
                            onClick = {
                                if (state.exportJsonString == null) {
                                    viewModel.generateJsonBackup()
                                } else {
                                    clipboardManager.setText(AnnotatedString(state.exportJsonString!!))
                                    Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "export_json_backup_btn"
                        )
                    }
                }
            }

            // CSV Export
            item {
                GymCard(backgroundColor = PrimaryCard) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Spreadsheet Exports (CSV)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Export workouts and body weight history for analysis in Excel or Google Sheets.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GymSecondaryButton(
                                text = "Workouts CSV",
                                icon = Icons.Default.Download,
                                onClick = {
                                    viewModel.generateWorkoutsCsv()
                                    if (state.exportWorkoutsCsvString != null) {
                                        clipboardManager.setText(AnnotatedString(state.exportWorkoutsCsvString!!))
                                        Toast.makeText(context, "Workouts CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            GymSecondaryButton(
                                text = "Weight CSV",
                                icon = Icons.Default.Download,
                                onClick = {
                                    viewModel.generateWeightCsv()
                                    if (state.exportWeightCsvString != null) {
                                        clipboardManager.setText(AnnotatedString(state.exportWeightCsvString!!))
                                        Toast.makeText(context, "Weight CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Restore Section
            item {
                Text(
                    text = "RESTORE BACKUP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MutedText,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            item {
                GymCard(backgroundColor = PrimaryCard) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Restore from JSON",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Restore all workouts, exercises, and profile records from a previously exported Gym Tracker JSON backup.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        GymSecondaryButton(
                            text = "Enter JSON to Restore",
                            icon = Icons.Default.FileDownload,
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "restore_backup_btn"
                        )

                        if (state.restoreMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = state.restoreMessage!!,
                                style = MaterialTheme.typography.bodySmall.copy(color = AccentSuccess)
                            )
                        }

                        if (state.restoreError != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = state.restoreError!!,
                                style = MaterialTheme.typography.bodySmall.copy(color = AccentError)
                            )
                        }
                    }
                }
            }
        }
    }

    // Input JSON Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = {
                Text(
                    text = "Paste Backup JSON",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryText)
                )
            },
            text = {
                OutlinedTextField(
                    value = restoreInputJson,
                    onValueChange = { restoreInputJson = it },
                    placeholder = { Text("Paste JSON backup string here...", color = MutedText) },
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SecondaryCard,
                        unfocusedContainerColor = PrimaryCard,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DividerDark,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            },
            confirmButton = {
                GymGradientButton(
                    text = "Validate & Restore",
                    onClick = {
                        if (restoreInputJson.isNotBlank()) {
                            showRestoreDialog = false
                            showConfirmRestoreDialog = true
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel", color = SecondaryText)
                }
            },
            containerColor = PrimaryCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Confirmation before Overwrite
    ConfirmationDialog(
        show = showConfirmRestoreDialog,
        title = "Confirm Restore?",
        message = "Restoring from backup will merge or update matching exercises and sessions. Proceed?",
        confirmText = "Proceed with Restore",
        isDestructive = false,
        onConfirm = {
            viewModel.restoreBackup(restoreInputJson) {
                Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_LONG).show()
            }
            showConfirmRestoreDialog = false
            restoreInputJson = ""
        },
        onDismiss = { showConfirmRestoreDialog = false }
    )
}
