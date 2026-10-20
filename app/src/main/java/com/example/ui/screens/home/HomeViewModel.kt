package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ScheduleDayDetail
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutSessionWithDetails
import com.example.data.model.WorkoutTemplate
import com.example.data.repository.GymRepository
import com.example.util.BodyMath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BentoRoutineInfo(
    val number: Int,
    val title: String,
    val dayLabel: String = "Monday",
    val scheduleSubtitle: String,
    val templateId: String?,
    val isRestDay: Boolean = false,
    val progress: Float = 0.0f
)

data class BodyMeasurementReminderState(
    val showBanner: Boolean = false,
    val title: String = "Update Body Details",
    val subtitle: String = "Baseline measurement due",
    val fieldsCompletedCount: Int = 0,
    val totalFieldsCount: Int = 10,
    val completionPercentage: Float = 0.0f
)

data class HomeUiState(
    val userProfile: UserProfile = UserProfile(),
    val activeSession: WorkoutSessionWithDetails? = null,
    val routine1: BentoRoutineInfo = BentoRoutineInfo(1, "Push Day", "Monday", "Chest, Shoulders & Triceps", null, false, 0.0f),
    val currentWeight: Double? = null,
    val weightSubtitle: String = "Tap to log",
    val weightDeltaStr: String? = null,
    val weightDeltaPositive: Boolean? = null,
    val bodyMeasurementReminder: BodyMeasurementReminderState = BodyMeasurementReminderState(),
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

        // Templates & Dynamic Schedule matching today's calendar day
        viewModelScope.launch {
            repository.scheduleDays.collect { scheduleDaysList ->
                val todayDbIndex = BodyMath.getDatabaseDayIndex()
                val todayDayName = BodyMath.getDayName(todayDbIndex)
                val todaySchedule = scheduleDaysList.firstOrNull { it.scheduleDay.dayIndex == todayDbIndex }

                val r1 = if (todaySchedule != null) {
                    if (todaySchedule.scheduleDay.isRestDay || todaySchedule.template == null) {
                        BentoRoutineInfo(
                            number = 1,
                            title = "Rest Day",
                            dayLabel = todayDayName,
                            scheduleSubtitle = "Scheduled Recovery & Muscle Repair",
                            templateId = null,
                            isRestDay = true,
                            progress = 1.0f
                        )
                    } else {
                        val tmpl = todaySchedule.template
                        BentoRoutineInfo(
                            number = 1,
                            title = tmpl.name,
                            dayLabel = todayDayName,
                            scheduleSubtitle = tmpl.description ?: "Targeted training session",
                            templateId = tmpl.id,
                            isRestDay = false,
                            progress = 0.0f
                        )
                    }
                } else {
                    // Fallback to first available template if schedule not customized
                    val tmpl = _uiState.value.templates.firstOrNull()
                    BentoRoutineInfo(
                        number = 1,
                        title = tmpl?.name ?: "Workout Session",
                        dayLabel = todayDayName,
                        scheduleSubtitle = tmpl?.description ?: "Scheduled routine",
                        templateId = tmpl?.id,
                        isRestDay = false,
                        progress = 0.0f
                    )
                }

                _uiState.value = _uiState.value.copy(routine1 = r1)
            }
        }

        // All Templates list
        viewModelScope.launch {
            repository.allTemplates.collect { list ->
                _uiState.value = _uiState.value.copy(templates = list)
            }
        }

        // Body weight + delta calculation
        viewModelScope.launch {
            repository.allBodyWeightEntries.collect { entries ->
                val latest = entries.getOrNull(0)
                val previous = entries.getOrNull(1)

                if (latest != null) {
                    val diffMin = ((System.currentTimeMillis() - latest.date) / 60000L).coerceAtLeast(0)
                    val sub = when {
                        diffMin < 60 -> "$diffMin min ago"
                        diffMin < 1440 -> "${diffMin / 60} hours ago"
                        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(latest.date))
                    }

                    val (deltaStr, deltaPos) = if (previous != null) {
                        val delta = latest.weight - previous.weight
                        val unit = _uiState.value.userProfile.preferredWeightUnit
                        val sign = if (delta > 0) "+" else ""
                        Pair(
                            "$sign${String.format(Locale.getDefault(), "%.1f", delta)} $unit",
                            delta >= 0
                        )
                    } else {
                        Pair(null, null)
                    }

                    _uiState.value = _uiState.value.copy(
                        currentWeight = latest.weight,
                        weightSubtitle = sub,
                        weightDeltaStr = deltaStr,
                        weightDeltaPositive = deltaPos
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        currentWeight = null,
                        weightSubtitle = "Tap to log",
                        weightDeltaStr = null,
                        weightDeltaPositive = null
                    )
                }
            }
        }

        // In-app Body Measurement Reminder
        viewModelScope.launch {
            repository.latestBodyMeasurement.collect { latestMeasurement ->
                if (latestMeasurement == null) {
                    _uiState.value = _uiState.value.copy(
                        bodyMeasurementReminder = BodyMeasurementReminderState(
                            showBanner = true,
                            title = "Update Body Details",
                            subtitle = "First baseline measurement due",
                            fieldsCompletedCount = 0,
                            totalFieldsCount = 10,
                            completionPercentage = 0.0f
                        )
                    )
                } else {
                    val daysSince = ((System.currentTimeMillis() - latestMeasurement.measuredAt) / (24L * 3600 * 1000)).coerceAtLeast(0)

                    // Count optional progress fields
                    var filled = 0
                    if (latestMeasurement.chestCm != null && latestMeasurement.chestCm > 0.0) filled++
                    if (latestMeasurement.shouldersCm != null && latestMeasurement.shouldersCm > 0.0) filled++
                    if (latestMeasurement.leftArmCm != null && latestMeasurement.leftArmCm > 0.0) filled++
                    if (latestMeasurement.rightArmCm != null && latestMeasurement.rightArmCm > 0.0) filled++
                    if (latestMeasurement.leftForearmCm != null && latestMeasurement.leftForearmCm > 0.0) filled++
                    if (latestMeasurement.rightForearmCm != null && latestMeasurement.rightForearmCm > 0.0) filled++
                    if (latestMeasurement.leftThighCm != null && latestMeasurement.leftThighCm > 0.0) filled++
                    if (latestMeasurement.rightThighCm != null && latestMeasurement.rightThighCm > 0.0) filled++
                    if (latestMeasurement.leftCalfCm != null && latestMeasurement.leftCalfCm > 0.0) filled++
                    if (latestMeasurement.rightCalfCm != null && latestMeasurement.rightCalfCm > 0.0) filled++

                    val pct = (filled.toFloat() / 10f).coerceIn(0f, 1f)
                    val reminderDays = (_uiState.value.userProfile.measurementReminderWeeks.coerceIn(1, 4)) * 7L
                    val reminderDue = daysSince >= reminderDays || pct < 1.0f

                    _uiState.value = _uiState.value.copy(
                        bodyMeasurementReminder = BodyMeasurementReminderState(
                            showBanner = reminderDue,
                            title = "Update Body Details",
                            subtitle = if (daysSince == 0L) "Updated today" else "Last measured $daysSince days ago",
                            fieldsCompletedCount = filled,
                            totalFieldsCount = 10,
                            completionPercentage = pct
                        )
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
