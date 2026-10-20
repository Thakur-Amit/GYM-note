package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.GymApplication
import com.example.ui.screens.body.ComprehensiveBodyDetailsScreen
import com.example.ui.screens.exercises.ExerciseDetailScreen
import com.example.ui.screens.exercises.ExerciseEditorScreen
import com.example.ui.screens.exercises.ExercisesScreen
import com.example.ui.screens.exercises.ExercisesViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.plan.PlanScreen
import com.example.ui.screens.plan.PlanViewModel
import com.example.ui.screens.plan.RoutineDetailScreen
import com.example.ui.screens.plan.ScheduleEditorScreen
import com.example.ui.screens.plan.TemplateEditorScreen
import com.example.ui.screens.profile.BackupRestoreScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.ProfileViewModel
import com.example.ui.screens.progress.BodyWeightScreen
import com.example.ui.screens.progress.ProgressScreen
import com.example.ui.screens.progress.ProgressViewModel
import com.example.ui.screens.progress.ReportsScreen
import com.example.ui.screens.workout.ActiveWorkoutScreen
import com.example.ui.screens.workout.ActiveWorkoutViewModel
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.DividerDark
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.MutedText
import com.example.ui.theme.PrimaryCard
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText

@Composable
fun GymAppRoot(
    navController: NavHostController = rememberNavController()
) {
    val app = LocalContext.current.applicationContext as GymApplication
    val repository = app.repository
    val backupManager = app.backupManager

    // Shared ViewModels
    val homeViewModel = remember { HomeViewModel(repository) }
    val exercisesViewModel = remember { ExercisesViewModel(repository) }
    val planViewModel = remember { PlanViewModel(repository) }
    val activeWorkoutViewModel = remember { ActiveWorkoutViewModel(repository) }
    val progressViewModel = remember { ProgressViewModel(repository) }
    val profileViewModel = remember { ProfileViewModel(repository, backupManager) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom navigation bar on all main tabs (and when initial route is resolving)
    val shouldShowBottomBar = currentRoute == null || currentRoute in Screen.bottomNavItems.map { it.route }

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = PrimaryCard,
                    contentColor = PrimaryText,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav")
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val isSelected = (currentRoute ?: Screen.Home.route) == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (screen == Screen.Home) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                } else {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(Screen.Home.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            },
                            icon = {
                                screen.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryText,
                                unselectedIconColor = SecondaryText,
                                selectedTextColor = PrimaryPurple,
                                unselectedTextColor = MutedText,
                                indicatorColor = PrimaryPurple
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Home Destination
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToActiveWorkout = {
                        navController.navigate(Screen.ActiveWorkout.route)
                    },
                    onNavigateToExercises = {
                        navController.navigate(Screen.Exercises.route)
                    },
                    onNavigateToProgress = {
                        navController.navigate(Screen.Progress.route)
                    },
                    onNavigateToReports = {
                        navController.navigate(Screen.Reports.route)
                    },
                    onNavigateToBodyWeight = {
                        navController.navigate(Screen.BodyWeight.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToRoutineDetail = { templateId ->
                        navController.navigate(Screen.RoutineDetail.createRoute(templateId))
                    },
                    onNavigateToBodyDetails = {
                        navController.navigate(Screen.BodyMeasurements.route)
                    }
                )
            }

            // Plan Destination
            composable(Screen.Plan.route) {
                PlanScreen(
                    viewModel = planViewModel,
                    onNavigateToActiveWorkout = {
                        navController.navigate(Screen.ActiveWorkout.route)
                    },
                    onNavigateToRoutineDetail = { templateId ->
                        navController.navigate(Screen.RoutineDetail.createRoute(templateId))
                    },
                    onNavigateToTemplateEditor = { templateId ->
                        navController.navigate(Screen.TemplateEditor.createRoute(templateId))
                    },
                    onNavigateToScheduleEditor = {
                        navController.navigate(Screen.ScheduleEditor.route)
                    }
                )
            }

            // Exercises Destination
            composable(Screen.Exercises.route) {
                ExercisesScreen(
                    viewModel = exercisesViewModel,
                    onNavigateToDetail = { id ->
                        navController.navigate(Screen.ExerciseDetail.createRoute(id))
                    },
                    onNavigateToCreate = {
                        navController.navigate(Screen.ExerciseEditor.createRoute(null))
                    }
                )
            }

            // Progress Destination
            composable(Screen.Progress.route) {
                ProgressScreen(
                    viewModel = progressViewModel,
                    onNavigateToBodyWeight = {
                        navController.navigate(Screen.BodyWeight.route)
                    },
                    onNavigateToReports = {
                        navController.navigate(Screen.Reports.route)
                    }
                )
            }

            // Profile Destination
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToBackupRestore = {
                        navController.navigate(Screen.BackupRestore.route)
                    },
                    onNavigateToBodyMeasurements = {
                        navController.navigate(Screen.BodyMeasurements.route)
                    }
                )
            }

            // Dedicated Active Workout Flow
            composable(Screen.ActiveWorkout.route) {
                ActiveWorkoutScreen(
                    viewModel = activeWorkoutViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Routine Detail Destination
            composable(
                route = Screen.RoutineDetail.route,
                arguments = listOf(navArgument("templateId") { type = NavType.StringType })
            ) { backStackEntry ->
                val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
                RoutineDetailScreen(
                    templateId = templateId,
                    repository = repository,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToExerciseDetail = { exerciseId, fromWorkout, routineName ->
                        navController.navigate(Screen.ExerciseDetail.createRoute(exerciseId, fromWorkout, routineName))
                    },
                    onStartWorkout = { tmplId ->
                        planViewModel.startWorkout(tmplId) {
                            navController.navigate(Screen.ActiveWorkout.route)
                        }
                    },
                    onEditTemplate = { tmplId ->
                        navController.navigate(Screen.TemplateEditor.createRoute(tmplId))
                    }
                )
            }

            // Exercise Detail Screen
            composable(
                route = Screen.ExerciseDetail.route,
                arguments = listOf(
                    navArgument("exerciseId") { type = NavType.StringType },
                    navArgument("fromWorkout") {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                    navArgument("routineName") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
                val fromWorkout = backStackEntry.arguments?.getBoolean("fromWorkout") ?: false
                val rawRoutineName = backStackEntry.arguments?.getString("routineName") ?: ""
                val decodedRoutineName = try {
                    java.net.URLDecoder.decode(rawRoutineName, "UTF-8")
                } catch (e: Exception) {
                    rawRoutineName
                }
                ExerciseDetailScreen(
                    exerciseId = exerciseId,
                    viewModel = exercisesViewModel,
                    fromWorkout = fromWorkout,
                    routineName = decodedRoutineName,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id ->
                        navController.navigate(Screen.ExerciseEditor.createRoute(id))
                    }
                )
            }

            // Exercise Editor Screen
            composable(
                route = Screen.ExerciseEditor.route,
                arguments = listOf(navArgument("exerciseId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId")
                ExerciseEditorScreen(
                    exerciseId = exerciseId,
                    viewModel = exercisesViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Template Editor Screen
            composable(
                route = Screen.TemplateEditor.route,
                arguments = listOf(navArgument("templateId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val templateId = backStackEntry.arguments?.getString("templateId")
                TemplateEditorScreen(
                    templateId = templateId,
                    viewModel = planViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Schedule Editor Screen
            composable(Screen.ScheduleEditor.route) {
                ScheduleEditorScreen(
                    viewModel = planViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Dedicated Body Weight Screen
            composable(Screen.BodyWeight.route) {
                BodyWeightScreen(
                    viewModel = progressViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Dedicated Reports Screen
            composable(Screen.Reports.route) {
                ReportsScreen(
                    viewModel = progressViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Backup & Restore Screen
            composable(Screen.BackupRestore.route) {
                BackupRestoreScreen(
                    viewModel = profileViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Comprehensive Body Details Screen (BMI, U.S. Navy Body Fat %, Circumferences)
            composable(Screen.ComprehensiveBodyDetails.route) {
                ComprehensiveBodyDetailsScreen(
                    repository = repository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Body Measurements Screen (Matches user design image d3c75c30)
            composable(Screen.BodyMeasurements.route) {
                val bodyMeasurementsViewModel = remember {
                    com.example.ui.screens.body.BodyMeasurementsViewModel(repository)
                }
                com.example.ui.screens.body.BodyMeasurementsScreen(
                    viewModel = bodyMeasurementsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
