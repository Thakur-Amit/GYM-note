package com.example.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Exercise
import com.example.data.model.ExercisePerformanceRecord
import com.example.data.model.WorkoutSession
import com.example.data.model.WorkoutSessionWithDetails
import com.example.data.model.WorkoutSet
import com.example.data.repository.GymRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActiveWorkoutUiState(
    val sessionWithDetails: WorkoutSessionWithDetails? = null,
    val previousPerformances: Map<String, ExercisePerformanceRecord> = emptyMap(),
    val availableExercises: List<Exercise> = emptyList(),
    val elapsedDurationSeconds: Long = 0L,
    val isSaving: Boolean = false,
    val saveStatus: String = "Saved",
    val completedSummary: WorkoutSession? = null,
    val showSummaryDialog: Boolean = false,
    val error: String? = null
)

class ActiveWorkoutViewModel(
    private val repository: GymRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        observeActiveSession()
        observeAvailableExercises()
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            repository.activeSession.collect { sessionDetails ->
                _uiState.value = _uiState.value.copy(
                    sessionWithDetails = sessionDetails,
                    saveStatus = "Saved"
                )

                if (sessionDetails != null) {
                    startElapsedTimer(sessionDetails.session.startTime)
                    loadPreviousPerformances(sessionDetails)
                } else {
                    timerJob?.cancel()
                }
            }
        }
    }

    private fun startElapsedTimer(startTime: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val elapsed = ((now - startTime) / 1000L).coerceAtLeast(0L)
                _uiState.value = _uiState.value.copy(elapsedDurationSeconds = elapsed)
                delay(1000L)
            }
        }
    }

    private fun loadPreviousPerformances(sessionDetails: WorkoutSessionWithDetails) {
        viewModelScope.launch {
            val map = mutableMapOf<String, ExercisePerformanceRecord>()
            for (ex in sessionDetails.exercises) {
                repository.getPreviousPerformanceForExercise(ex.exercise.exerciseId).collect { prev ->
                    if (prev != null) {
                        map[ex.exercise.exerciseId] = prev
                        _uiState.value = _uiState.value.copy(previousPerformances = map.toMap())
                    }
                }
            }
        }
    }

    private fun observeAvailableExercises() {
        viewModelScope.launch {
            repository.activeExercises.collect { list ->
                _uiState.value = _uiState.value.copy(availableExercises = list)
            }
        }
    }

    fun addSet(sessionExerciseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saveStatus = "Saving...")
            repository.addSetToExercise(sessionExerciseId)
        }
    }

    fun removeSet(setId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saveStatus = "Saving...")
            repository.removeSet(setId)
        }
    }

    fun updateSetValues(set: WorkoutSet, weight: Double?, reps: Int?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saveStatus = "Saving...")
            val updated = set.copy(weight = weight, reps = reps)
            repository.updateSet(updated)
        }
    }

    fun completeSet(setId: String, completed: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saveStatus = "Saving...")
            repository.completeSet(setId, completed)
        }
    }

    fun addExerciseToWorkout(exerciseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saveStatus = "Saving...")
            repository.addExerciseToActiveWorkout(exerciseId)
        }
    }

    fun removeExerciseFromWorkout(sessionExerciseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saveStatus = "Saving...")
            repository.removeExerciseFromActiveWorkout(sessionExerciseId)
        }
    }

    fun finishWorkout(onFinished: () -> Unit) {
        viewModelScope.launch {
            timerJob?.cancel()
            val finished = repository.finishActiveWorkout()
            if (finished != null) {
                _uiState.value = _uiState.value.copy(
                    completedSummary = finished,
                    showSummaryDialog = true
                )
            } else {
                onFinished()
            }
        }
    }

    fun dismissSummary(onDismiss: () -> Unit) {
        _uiState.value = _uiState.value.copy(showSummaryDialog = false, completedSummary = null)
        onDismiss()
    }

    fun discardWorkout(onDiscarded: () -> Unit) {
        viewModelScope.launch {
            timerJob?.cancel()
            repository.discardActiveWorkout()
            onDiscarded()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
