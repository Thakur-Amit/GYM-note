package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ScheduleDayDetail
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutSessionWithDetails
import com.example.data.model.WorkoutTemplate
import com.example.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class BentoRoutineInfo(
    val number: Int,
    val title: String,
    val scheduleSubtitle: String,
    val templateId: String?,
    val progress: Float = 0.45f
)

data class HomeUiState(
    val userProfile: UserProfile = UserProfile(),
    val activeSession: WorkoutSessionWithDetails? = null,
    val routine1: BentoRoutineInfo = BentoRoutineInfo(1, "Push Day", "Chest, Shoulders & Triceps", null, 0.0f),
    val routine2: BentoRoutineInfo = BentoRoutineInfo(2, "Pull Day", "Back, Biceps & Core", null, 0.0f),
    val currentWeight: Double? = null,
    val weightSubtitle: String = "Tap to log",
    val volumeLifted7Days: Double = 0.0,
    val completedWorkoutDays: Set<Long> = emptySet(),
    val templates: List<WorkoutTemplate> = emptyList(),
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: GymRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        // User profile
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                profile?.let {
                    _uiState.value = _uiState.value.copy(userProfile = it)
                }
            }
        }

        // Active session
        viewModelScope.launch {
            repository.activeSession.collect { session ->
                _uiState.value = _uiState.value.copy(activeSession = session)
            }
        }

        // Templates & Schedule routines
        viewModelScope.launch {
            repository.allTemplates.collect { list ->
                _uiState.value = _uiState.value.copy(templates = list)
                val t1 = list.getOrNull(0)
                val t2 = list.getOrNull(1)

                val r1 = BentoRoutineInfo(
                    number = 1,
                    title = t1?.name ?: "Push Day",
                    scheduleSubtitle = t1?.description ?: "Chest, Shoulders & Triceps",
                    templateId = t1?.id,
                    progress = 0f
                )

                val r2 = BentoRoutineInfo(
                    number = 2,
                    title = t2?.name ?: "Pull Day",
                    scheduleSubtitle = t2?.description ?: "Back, Biceps & Posterior chain",
                    templateId = t2?.id,
                    progress = 0f
                )

                _uiState.value = _uiState.value.copy(
                    routine1 = r1,
                    routine2 = r2
                )
            }
        }

        // Body weight
        viewModelScope.launch {
            repository.allBodyWeightEntries.collect { entries ->
                val latest = entries.firstOrNull()
                if (latest != null) {
                    val diffMin = ((System.currentTimeMillis() - latest.date) / 60000L).coerceAtLeast(0)
                    val sub = when {
                        diffMin < 60 -> "$diffMin min ago"
                        diffMin < 1440 -> "${diffMin / 60} hours ago"
                        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(latest.date))
                    }
                    _uiState.value = _uiState.value.copy(
                        currentWeight = latest.weight,
                        weightSubtitle = sub
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        currentWeight = null,
                        weightSubtitle = "Tap to log"
                    )
                }
            }
        }

        // Completed sessions timestamps & 7-day volume
        viewModelScope.launch {
            repository.completedSessions.collect { sessions ->
                val timestamps = sessions.map { it.startTime }.toSet()

                val now = System.currentTimeMillis()
                val sevenDaysAgo = now - (7L * 24 * 3600 * 1000)
                val vol7 = sessions.filter { it.startTime in sevenDaysAgo..now }.sumOf { it.totalVolume }

                _uiState.value = _uiState.value.copy(
                    completedWorkoutDays = timestamps,
                    volumeLifted7Days = vol7
                )
            }
        }
    }

    fun startRoutine(templateId: String?, onStarted: () -> Unit) {
        viewModelScope.launch {
            if (templateId != null) {
                repository.startWorkoutFromTemplate(templateId)
            } else {
                val firstTmpl = _uiState.value.templates.firstOrNull()?.id
                if (firstTmpl != null) {
                    repository.startWorkoutFromTemplate(firstTmpl)
                } else {
                    repository.startAdHocWorkout("Workout")
                }
            }
            onStarted()
        }
    }

    fun startAdHocWorkout(onStarted: () -> Unit) {
        viewModelScope.launch {
            repository.startAdHocWorkout("Workout")
            onStarted()
        }
    }

    fun logBodyWeight(
        weight: Double,
        bodyFat: Double? = null,
        muscleMass: Double? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            repository.addBodyWeightEntry(
                weight = weight,
                unit = _uiState.value.userProfile.preferredWeightUnit,
                bodyFatPercentage = bodyFat,
                muscleMass = muscleMass,
                date = System.currentTimeMillis(),
                notes = notes
            )
        }
    }

    fun discardActiveWorkout() {
        viewModelScope.launch {
            repository.discardActiveWorkout()
        }
    }
}
