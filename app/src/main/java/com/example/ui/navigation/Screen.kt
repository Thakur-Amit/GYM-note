package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Plan : Screen("plan", "Workout", Icons.Default.FitnessCenter)
    object Exercises : Screen("exercises", "Exercises", Icons.Default.CalendarMonth)
    object Progress : Screen("progress", "Progress", Icons.Default.ShowChart)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)

    // Sub-screens
    object ActiveWorkout : Screen("workout/active", "Active Workout")
    object ExerciseDetail : Screen("exercise/{exerciseId}", "Exercise Details") {
        fun createRoute(exerciseId: String) = "exercise/$exerciseId"
    }
    object ExerciseEditor : Screen("exercise/editor?exerciseId={exerciseId}", "Exercise Editor") {
        fun createRoute(exerciseId: String? = null) = if (exerciseId != null) "exercise/editor?exerciseId=$exerciseId" else "exercise/editor"
    }
    object TemplateEditor : Screen("template/editor?templateId={templateId}", "Template Editor") {
        fun createRoute(templateId: String? = null) = if (templateId != null) "template/editor?templateId=$templateId" else "template/editor"
    }
    object ScheduleEditor : Screen("schedule/editor", "Schedule Editor")
    object BodyWeight : Screen("bodyweight", "Body Weight")
    object Reports : Screen("reports", "Progress Reports")
    object BackupRestore : Screen("backup_restore", "Backup & Restore")

    companion object {
        val bottomNavItems = listOf(Home, Plan, Exercises, Progress, Profile)
    }
}
