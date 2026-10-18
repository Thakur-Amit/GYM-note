package com.example.ui.screens.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Exercise
import com.example.data.model.ScheduleDay
import com.example.data.model.ScheduleDayDetail
import com.example.data.model.TemplateExercise
import com.example.data.model.TemplateExerciseDetail
import com.example.data.model.WorkoutSchedule
import com.example.data.model.WorkoutTemplate
import com.example.data.model.WorkoutTemplateWithExercises
import com.example.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class PlanUiState(
    val templates: List<WorkoutTemplate> = emptyList(),
    val scheduleDays: List<ScheduleDayDetail> = emptyList(),
    val activeSchedule: WorkoutSchedule? = null,
    val selectedTab: Int = 0, // 0 = Schedule, 1 = Templates
    val isLoading: Boolean = false
)

data class TemplateEditorUiState(
    val templateId: String? = null,
    val name: String = "",
    val description: String = "",
    val exercises: List<TemplateExerciseItem> = emptyList(),
    val availableExercises: List<Exercise> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null
)

data class TemplateExerciseItem(
    val exerciseId: String,
    val exerciseName: String,
    val primaryMuscleGroup: String,
    val imagePath: String?,
    val targetSets: Int = 3,
    val targetRepRange: String = "8-12",
    val targetWeight: Double? = null,
    val notes: String? = null
)

class PlanViewModel(
    private val repository: GymRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanUiState())
    val uiState: StateFlow<PlanUiState> = _uiState.asStateFlow()

    private val _editorState = MutableStateFlow(TemplateEditorUiState())
    val editorState: StateFlow<TemplateEditorUiState> = _editorState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.allTemplates.collect { list ->
                _uiState.value = _uiState.value.copy(templates = list)
            }
        }
        viewModelScope.launch {
            repository.scheduleDays.collect { days ->
                _uiState.value = _uiState.value.copy(scheduleDays = days)
            }
        }
        viewModelScope.launch {
            repository.activeSchedule.collect { schedule ->
                _uiState.value = _uiState.value.copy(activeSchedule = schedule)
            }
        }
    }

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun duplicateTemplate(templateId: String) {
        viewModelScope.launch {
            repository.duplicateTemplate(templateId)
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            repository.deleteTemplate(templateId)
        }
    }

    fun startWorkout(templateId: String, onStarted: () -> Unit) {
        viewModelScope.launch {
            repository.startWorkoutFromTemplate(templateId)
            onStarted()
        }
    }

    fun toggleScheduleDayRest(dayIndex: Int) {
        val currentDay = _uiState.value.scheduleDays.firstOrNull { it.scheduleDay.dayIndex == dayIndex } ?: return
        val updated = currentDay.scheduleDay.copy(
            isRestDay = !currentDay.scheduleDay.isRestDay,
            templateId = if (!currentDay.scheduleDay.isRestDay) null else _uiState.value.templates.firstOrNull()?.id
        )
        viewModelScope.launch {
            repository.updateScheduleDay(updated)
        }
    }

    fun assignTemplateToDay(dayIndex: Int, templateId: String?) {
        val currentDay = _uiState.value.scheduleDays.firstOrNull { it.scheduleDay.dayIndex == dayIndex } ?: return
        val updated = currentDay.scheduleDay.copy(
            isRestDay = templateId == null,
            templateId = templateId
        )
        viewModelScope.launch {
            repository.updateScheduleDay(updated)
        }
    }

    // Template Editor
    fun prepareTemplateEditor(templateId: String? = null) {
        viewModelScope.launch {
            val allExercises = mutableListOf<Exercise>()
            repository.activeExercises.collect { list ->
                allExercises.clear()
                allExercises.addAll(list)
                if (templateId != null) {
                    repository.getTemplateWithExercises(templateId).collect { details ->
                        if (details != null) {
                            val items = details.exercises.map { ed ->
                                TemplateExerciseItem(
                                    exerciseId = ed.templateExercise.exerciseId,
                                    exerciseName = ed.exercise?.name ?: "Exercise",
                                    primaryMuscleGroup = ed.exercise?.primaryMuscleGroup ?: "Other",
                                    imagePath = ed.exercise?.imagePath,
                                    targetSets = ed.templateExercise.targetSets,
                                    targetRepRange = ed.templateExercise.targetRepRange,
                                    targetWeight = ed.templateExercise.targetWeight,
                                    notes = ed.templateExercise.notes
                                )
                            }
                            _editorState.value = TemplateEditorUiState(
                                templateId = details.template.id,
                                name = details.template.name,
                                description = details.template.description ?: "",
                                exercises = items,
                                availableExercises = allExercises
                            )
                        }
                    }
                } else {
                    _editorState.value = TemplateEditorUiState(
                        templateId = null,
                        availableExercises = allExercises
                    )
                }
            }
        }
    }

    fun onEditorNameChanged(v: String) { _editorState.value = _editorState.value.copy(name = v) }
    fun onEditorDescriptionChanged(v: String) { _editorState.value = _editorState.value.copy(description = v) }

    fun addExerciseToTemplate(exercise: Exercise) {
        val current = _editorState.value.exercises.toMutableList()
        current.add(
            TemplateExerciseItem(
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                primaryMuscleGroup = exercise.primaryMuscleGroup,
                imagePath = exercise.imagePath,
                targetSets = 3,
                targetRepRange = "8-12",
                targetWeight = null
            )
        )
        _editorState.value = _editorState.value.copy(exercises = current)
    }

    fun removeExerciseFromTemplate(index: Int) {
        val current = _editorState.value.exercises.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _editorState.value = _editorState.value.copy(exercises = current)
        }
    }

    fun updateTemplateExerciseSets(index: Int, sets: Int) {
        val current = _editorState.value.exercises.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(targetSets = sets)
            _editorState.value = _editorState.value.copy(exercises = current)
        }
    }

    fun updateTemplateExerciseReps(index: Int, reps: String) {
        val current = _editorState.value.exercises.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(targetRepRange = reps)
            _editorState.value = _editorState.value.copy(exercises = current)
        }
    }

    fun saveTemplate(onSaved: () -> Unit) {
        val s = _editorState.value
        if (s.name.isBlank()) {
            _editorState.value = s.copy(error = "Template name is required")
            return
        }

        viewModelScope.launch {
            _editorState.value = s.copy(isSaving = true)
            val templateId = s.templateId ?: "template_${UUID.randomUUID().toString().take(8)}"
            val template = WorkoutTemplate(
                id = templateId,
                name = s.name.trim(),
                description = s.description.trim().ifEmpty { null },
                isCustom = true
            )
            val templateExercises = s.exercises.mapIndexed { index, item ->
                TemplateExercise(
                    templateId = templateId,
                    exerciseId = item.exerciseId,
                    orderIndex = index,
                    targetSets = item.targetSets,
                    targetRepRange = item.targetRepRange,
                    targetWeight = item.targetWeight,
                    notes = item.notes
                )
            }
            repository.saveTemplate(template, templateExercises)
            _editorState.value = _editorState.value.copy(isSaving = false)
            onSaved()
        }
    }
}
