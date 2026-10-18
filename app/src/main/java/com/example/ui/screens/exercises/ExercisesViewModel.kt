package com.example.ui.screens.exercises

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Exercise
import com.example.data.model.ExercisePRSummary
import com.example.data.model.ExercisePerformanceRecord
import com.example.data.model.MuscleGroup
import com.example.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExercisesUiState(
    val exercises: List<Exercise> = emptyList(),
    val filteredExercises: List<Exercise> = emptyList(),
    val selectedMuscleGroup: String? = null,
    val searchQuery: String = "",
    val muscleGroupCounts: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false
)

data class ExerciseDetailUiState(
    val exercise: Exercise? = null,
    val prSummary: ExercisePRSummary = ExercisePRSummary(),
    val history: List<ExercisePerformanceRecord> = emptyList(),
    val selectedTab: Int = 0, // 0 = History, 1 = Details/Instructions
    val isLoading: Boolean = false
)

data class ExerciseEditorUiState(
    val exerciseId: String? = null,
    val name: String = "",
    val primaryMuscleGroup: String = MuscleGroup.CHEST.name,
    val secondaryMuscleGroups: String = "",
    val exerciseType: String = "BARBELL",
    val equipment: String = "Barbell",
    val instructions: String = "",
    val loadEntryMode: String = "WEIGHT_AND_REPS",
    val notes: String = "",
    val currentImagePath: String? = null,
    val pendingImageUri: Uri? = null,
    val isImageRemoved: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

class ExercisesViewModel(
    private val repository: GymRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExercisesUiState())
    val uiState: StateFlow<ExercisesUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow(ExerciseDetailUiState())
    val detailState: StateFlow<ExerciseDetailUiState> = _detailState.asStateFlow()

    private val _editorState = MutableStateFlow(ExerciseEditorUiState())
    val editorState: StateFlow<ExerciseEditorUiState> = _editorState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.activeExercises.collect { list ->
                val counts = list.groupBy { it.primaryMuscleGroup }
                    .mapValues { it.value.size }
                _uiState.value = _uiState.value.copy(
                    exercises = list,
                    muscleGroupCounts = counts
                )
                applyFilter()
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilter()
    }

    fun onMuscleGroupSelected(muscleGroup: String?) {
        _uiState.value = _uiState.value.copy(selectedMuscleGroup = muscleGroup)
        applyFilter()
    }

    private fun applyFilter() {
        val current = _uiState.value
        val filtered = current.exercises.filter { ex ->
            val matchesGroup = current.selectedMuscleGroup == null || ex.primaryMuscleGroup == current.selectedMuscleGroup
            val matchesQuery = current.searchQuery.isBlank() ||
                    ex.name.contains(current.searchQuery, ignoreCase = true) ||
                    ex.primaryMuscleGroup.contains(current.searchQuery, ignoreCase = true) ||
                    ex.equipment.contains(current.searchQuery, ignoreCase = true)
            matchesGroup && matchesQuery
        }
        _uiState.value = current.copy(filteredExercises = filtered)
    }

    // Detail Screen operations
    fun loadExerciseDetails(exerciseId: String) {
        viewModelScope.launch {
            repository.getExerciseById(exerciseId).collect { ex ->
                _detailState.value = _detailState.value.copy(exercise = ex)
            }
        }
        viewModelScope.launch {
            repository.getExercisePRSummary(exerciseId).collect { pr ->
                _detailState.value = _detailState.value.copy(prSummary = pr)
            }
        }
        viewModelScope.launch {
            repository.getExerciseHistory(exerciseId).collect { history ->
                _detailState.value = _detailState.value.copy(history = history)
            }
        }
    }

    fun setDetailTab(tabIndex: Int) {
        _detailState.value = _detailState.value.copy(selectedTab = tabIndex)
    }

    fun archiveExercise(exerciseId: String, onArchived: () -> Unit) {
        viewModelScope.launch {
            repository.setExerciseArchived(exerciseId, true)
            onArchived()
        }
    }

    // Editor operations
    fun prepareEditor(exerciseId: String? = null) {
        if (exerciseId != null) {
            viewModelScope.launch {
                repository.getExerciseById(exerciseId).collect { ex ->
                    if (ex != null) {
                        _editorState.value = ExerciseEditorUiState(
                            exerciseId = ex.id,
                            name = ex.name,
                            primaryMuscleGroup = ex.primaryMuscleGroup,
                            secondaryMuscleGroups = ex.secondaryMuscleGroups,
                            exerciseType = ex.exerciseType,
                            equipment = ex.equipment,
                            instructions = ex.instructions ?: "",
                            loadEntryMode = ex.loadEntryMode,
                            notes = ex.notes ?: "",
                            currentImagePath = ex.imagePath
                        )
                    }
                }
            }
        } else {
            _editorState.value = ExerciseEditorUiState()
        }
    }

    fun onEditorNameChanged(v: String) { _editorState.value = _editorState.value.copy(name = v) }
    fun onEditorMuscleGroupChanged(v: String) { _editorState.value = _editorState.value.copy(primaryMuscleGroup = v) }
    fun onEditorEquipmentChanged(v: String) { _editorState.value = _editorState.value.copy(equipment = v) }
    fun onEditorInstructionsChanged(v: String) { _editorState.value = _editorState.value.copy(instructions = v) }
    fun onEditorNotesChanged(v: String) { _editorState.value = _editorState.value.copy(notes = v) }
    fun onEditorImagePicked(uri: Uri) {
        _editorState.value = _editorState.value.copy(pendingImageUri = uri, isImageRemoved = false)
    }
    fun onEditorImageRemoved() {
        _editorState.value = _editorState.value.copy(pendingImageUri = null, isImageRemoved = true, currentImagePath = null)
    }

    fun saveExercise(onSaved: () -> Unit) {
        val s = _editorState.value
        if (s.name.isBlank()) {
            _editorState.value = s.copy(error = "Exercise name is required")
            return
        }

        viewModelScope.launch {
            _editorState.value = s.copy(isSaving = true, error = null)
            val exerciseId = s.exerciseId ?: "custom_${java.util.UUID.randomUUID().toString().take(8)}"
            val exercise = Exercise(
                id = exerciseId,
                name = s.name.trim(),
                primaryMuscleGroup = s.primaryMuscleGroup,
                secondaryMuscleGroups = s.secondaryMuscleGroups,
                exerciseType = s.exerciseType,
                equipment = s.equipment.trim(),
                instructions = s.instructions.trim().ifEmpty { null },
                imagePath = if (s.isImageRemoved) null else s.currentImagePath,
                loadEntryMode = s.loadEntryMode,
                notes = s.notes.trim().ifEmpty { null },
                isCustom = true
            )
            repository.saveExercise(exercise, s.pendingImageUri)
            _editorState.value = _editorState.value.copy(isSaving = false)
            onSaved()
        }
    }
}
