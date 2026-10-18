package com.example.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GymCard
import com.example.ui.components.GymGradientButton
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.DividerDark
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryCard
import com.example.ui.theme.SecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditorScreen(
    viewModel: PlanViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Weekly Schedule") },
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
                .testTag("schedule_editor_screen"),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Assign a workout template or rest day to each day of the week:",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SecondaryText),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(state.scheduleDays) { dayDetail ->
                val day = dayDetail.scheduleDay
                var expanded by remember { mutableStateOf(false) }

                GymCard(backgroundColor = PrimaryCard, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = day.dayName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (day.isRestDay) "Rest Day" else dayDetail.template?.name ?: "No template",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (day.isRestDay) SecondaryText else PrimaryPurple
                                )
                            )
                        }

                        // Dropdown anchor
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SecondaryCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DividerDark),
                            modifier = Modifier.clickable { expanded = true }
                        ) {
                            Text(
                                text = "Change",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = PrimaryText,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(PrimaryCard)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Rest Day", color = SecondaryText) },
                                    onClick = {
                                        viewModel.assignTemplateToDay(day.dayIndex, null)
                                        expanded = false
                                    }
                                )
                                state.templates.forEach { tmpl ->
                                    DropdownMenuItem(
                                        text = { Text(tmpl.name, color = PrimaryText) },
                                        onClick = {
                                            viewModel.assignTemplateToDay(day.dayIndex, tmpl.id)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                GymGradientButton(
                    text = "Done",
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "done_schedule_editor_btn"
                )
            }
        }
    }
}
