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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WorkoutTemplate
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.GymCard
import com.example.ui.theme.AccentError
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.DividerDark
import com.example.ui.theme.MutedText
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryCard
import com.example.ui.theme.SecondaryText

@Composable
fun PlanScreen(
    viewModel: PlanViewModel,
    onNavigateToActiveWorkout: () -> Unit,
    onNavigateToTemplateEditor: (String?) -> Unit,
    onNavigateToScheduleEditor: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var templateToDelete by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var selectedPlanTab by remember { mutableStateOf(0) } // 0 = Templates, 1 = Custom

    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToTemplateEditor(null) },
                containerColor = PrimaryPurple,
                contentColor = PrimaryText,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("create_template_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create workout template"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundDark)
                .testTag("plan_screen")
        ) {
            // Header matching Image 1 Screen 7
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "Start Workout",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented Tabs: [Templates] [Custom] matching Image 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PrimaryCard)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tabs = listOf("Templates", "Custom")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedPlanTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PrimaryPurple else Color.Transparent)
                                .clickable { selectedPlanTab = index },
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
            }

            val templatesToShow = if (selectedPlanTab == 0) {
                state.templates
            } else {
                state.templates.filter { it.isCustom }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(templatesToShow) { template ->
                    TemplateItemCardImage1(
                        template = template,
                        onStartWorkout = {
                            viewModel.startWorkout(template.id, onNavigateToActiveWorkout)
                        },
                        onEdit = { onNavigateToTemplateEditor(template.id) },
                        onDuplicate = { viewModel.duplicateTemplate(template.id) },
                        onDelete = { templateToDelete = template }
                    )
                }
            }
        }
    }

    templateToDelete?.let { tmpl ->
        ConfirmationDialog(
            show = true,
            title = "Delete Template?",
            message = "Are you sure you want to delete '${tmpl.name}'? Past completed workouts will remain preserved.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteTemplate(tmpl.id)
                templateToDelete = null
            },
            onDismiss = { templateToDelete = null }
        )
    }
}

@Composable
fun TemplateItemCardImage1(
    template: WorkoutTemplate,
    onStartWorkout: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    GymCard(
        backgroundColor = PrimaryCard,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onStartWorkout)
            .testTag("template_card_${template.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = template.description ?: "Targeted training session",
                    style = MaterialTheme.typography.bodySmall.copy(color = SecondaryText)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = MutedText, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MutedText, modifier = Modifier.size(16.dp))
                }
                if (template.isCustom) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentError, modifier = Modifier.size(16.dp))
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Start",
                    tint = SecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
