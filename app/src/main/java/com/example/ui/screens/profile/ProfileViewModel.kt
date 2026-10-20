package com.example.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.model.UserProfile
import com.example.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val calculatedAge: Int? = null,
    val isSaving: Boolean = false,
    val exportJsonString: String? = null,
    val exportWorkoutsCsvString: String? = null,
    val exportWeightCsvString: String? = null,
    val exportMeasurementsCsvString: String? = null,
    val restoreMessage: String? = null,
    val restoreError: String? = null
)

class ProfileViewModel(
    private val repository: GymRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                if (profile != null) {
                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        calculatedAge = profile.calculateAge()
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        val updated = _uiState.value.profile.copy(name = name)
        saveProfile(updated)
    }

    fun updateHeight(heightCm: Double) {
        val updated = _uiState.value.profile.copy(heightCm = heightCm)
        saveProfile(updated)
    }

    fun updateDateOfBirth(dobMillis: Long) {
        val updated = _uiState.value.profile.copy(dateOfBirth = dobMillis)
        saveProfile(updated)
    }

    fun updatePreferredWeightUnit(unit: String) {
        val updated = _uiState.value.profile.copy(preferredWeightUnit = unit)
        saveProfile(updated)
    }

    fun updateFitnessGoal(goal: String) {
        val updated = _uiState.value.profile.copy(fitnessGoal = goal)
        saveProfile(updated)
    }

    fun updateNotes(notes: String) {
        val updated = _uiState.value.profile.copy(personalNotes = notes)
        saveProfile(updated)
    }

    fun updateMeasurementReminderWeeks(weeks: Int) {
        val updated = _uiState.value.profile.copy(measurementReminderWeeks = weeks.coerceIn(1, 4))
        saveProfile(updated)
    }

    fun setProfilePicture(uri: Uri) {
        viewModelScope.launch {
            repository.setProfilePicture(uri)
        }
    }

    fun removeProfilePicture() {
        viewModelScope.launch {
            repository.removeProfilePicture()
        }
    }

    private fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    // Backup & Restore
    fun generateJsonBackup() {
        viewModelScope.launch {
            val json = backupManager.exportToJson()
            _uiState.value = _uiState.value.copy(exportJsonString = json)
        }
    }

    fun generateWorkoutsCsv() {
        viewModelScope.launch {
            val csv = backupManager.exportWorkoutsToCsv()
            _uiState.value = _uiState.value.copy(exportWorkoutsCsvString = csv)
        }
    }

    fun generateWeightCsv() {
        viewModelScope.launch {
            val csv = backupManager.exportBodyWeightToCsv()
            _uiState.value = _uiState.value.copy(exportWeightCsvString = csv)
        }
    }

    fun generateMeasurementsCsv() {
        viewModelScope.launch {
            val csv = backupManager.exportBodyMeasurementsToCsv()
            _uiState.value = _uiState.value.copy(exportMeasurementsCsvString = csv)
        }
    }

    fun restoreBackup(jsonString: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val result = backupManager.restoreFromJson(jsonString)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    restoreMessage = result.getOrNull(),
                    restoreError = null
                )
                onComplete()
            } else {
                _uiState.value = _uiState.value.copy(
                    restoreError = result.exceptionOrNull()?.message ?: "Restore failed",
                    restoreMessage = null
                )
            }
        }
    }
}
