package com.example.ui.screens.progress

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BodyWeightEntry
import com.example.data.model.ProgressSummary
import com.example.data.model.WorkoutSession
import com.example.data.repository.GymRepository
import com.example.ui.components.ChartBarData
import com.example.ui.components.ChartLinePoint
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.PrimaryPurple
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class TimeRange(val label: String, val days: Int) {
    ONE_WEEK("1W", 7),
    ONE_MONTH("1M", 30),
    THREE_MONTHS("3M", 90),
    SIX_MONTHS("6M", 180),
    ONE_YEAR("1Y", 365),
    ALL_TIME("All", 3650)
}

data class MuscleDistributionItem(
    val muscleGroup: String,
    val volume: Double,
    val percentage: Int,
    val color: Color
)

data class ProgressUiState(
    val selectedRange: TimeRange = TimeRange.ONE_MONTH,
    val summary: ProgressSummary? = null,
    val volumeChartData: List<ChartBarData> = emptyList(),
    val weightChartData: List<ChartLinePoint> = emptyList(),
    val recentSessions: List<WorkoutSession> = emptyList(),
    val weightEntries: List<BodyWeightEntry> = emptyList(),
    val latestWeight: Double? = null,
    val previousWeight: Double? = null,
    val weightDelta: Double? = null,
    val preferredUnit: String = "kg",
    val heightCm: Double? = null,
    val userAge: Int? = null,
    val latestBmi: Double? = null,
    val latestBmiCategory: String? = null,
    val latestBodyFat: Double? = null,
    val latestMuscleMass: Double? = null,
    val avgWeightLifted: Double? = null,
    val avgReps: Double? = null,
    val muscleDistributions: List<MuscleDistributionItem> = emptyList(),
    val isLoading: Boolean = false
)

data class ReportsUiState(
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH), // 0-indexed
    val currentMonthSummary: ProgressSummary? = null,
    val previousMonthSummary: ProgressSummary? = null,
    val volumeDeltaPercent: Double? = null,
    val highestVolumeMonth: String? = null,
    val lowestVolumeMonth: String? = null,
    val monthlyBars: List<ChartBarData> = emptyList(),
    val isLoading: Boolean = false
)

class ProgressViewModel(
    private val repository: GymRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val _reportsState = MutableStateFlow(ReportsUiState())
    val reportsState: StateFlow<ReportsUiState> = _reportsState.asStateFlow()

    init {
        loadData()
        loadReports()
    }

    fun onTimeRangeSelected(range: TimeRange) {
        _uiState.value = _uiState.value.copy(selectedRange = range)
        loadData()
    }

    private fun loadData() {
        val range = _uiState.value.selectedRange
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (range.days.toLong() * 24 * 3600 * 1000)

        // Observe profile unit & physical measurements
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                profile?.let {
                    val age = it.calculateAge()
                    val height = it.heightCm
                    val latestEntry = _uiState.value.weightEntries.firstOrNull()

                    val bmi = latestEntry?.calculateBmi(height)
                    val bmiCat = latestEntry?.getBmiCategory(height)
                    val bf = latestEntry?.calculateBodyFatPercentage(height, age)
                    val mm = latestEntry?.calculateMuscleMass(height, age)

                    _uiState.value = _uiState.value.copy(
                        preferredUnit = it.preferredWeightUnit,
                        heightCm = height,
                        userAge = age,
                        latestBmi = bmi,
                        latestBmiCategory = bmiCat,
                        latestBodyFat = bf,
                        latestMuscleMass = mm
                    )
                }
            }
        }

        // Summary for selected time range
        viewModelScope.launch {
            repository.getProgressSummary(startTime, endTime).collect { summary ->
                // Compute muscle distributions
                val muscleMap = summary.muscleGroupVolumeMap
                val totalVol = summary.totalVolume

                val items = muscleMap.mapNotNull { (muscle, vol) ->
                    if (vol <= 0) null
                    else {
                        val pct = if (totalVol > 0) ((vol / totalVol) * 100).roundToInt() else 0
                        val color = when (muscle.lowercase()) {
                            "chest" -> NeonRose
                            "back" -> NeonCyan
                            "shoulders" -> NeonAmber
                            "biceps" -> NeonEmerald
                            "triceps" -> NeonViolet
                            "legs", "quads", "hamstrings" -> Color(0xFF10B981)
                            "core/abs", "core", "abs" -> Color(0xFFEAB308)
                            "glutes" -> Color(0xFFEC4899)
                            "calves" -> Color(0xFF06B6D4)
                            else -> PrimaryPurple
                        }
                        MuscleDistributionItem(
                            muscleGroup = muscle,
                            volume = vol,
                            percentage = pct,
                            color = color
                        )
                    }
                }.sortedByDescending { it.volume }

                _uiState.value = _uiState.value.copy(
                    summary = summary,
                    muscleDistributions = items
                )
            }
        }

        // Body Weight entries & composition tracking
        viewModelScope.launch {
            repository.allBodyWeightEntries.collect { entries ->
                val latest = entries.firstOrNull()
                val prev = entries.getOrNull(1)
                val delta = if (latest != null && prev != null) latest.weight - prev.weight else null

                val height = _uiState.value.heightCm
                val age = _uiState.value.userAge
                val bmi = latest?.calculateBmi(height)
                val bmiCat = latest?.getBmiCategory(height)
                val bf = latest?.calculateBodyFatPercentage(height, age)
                val mm = latest?.calculateMuscleMass(height, age)

                val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                val points = entries.take(15).reversed().map {
                    ChartLinePoint(
                        label = sdf.format(Date(it.date)),
                        value = it.weight,
                        timestamp = it.date
                    )
                }

                _uiState.value = _uiState.value.copy(
                    weightEntries = entries,
                    latestWeight = latest?.weight,
                    previousWeight = prev?.weight,
                    weightDelta = delta,
                    weightChartData = points,
                    latestBmi = bmi,
                    latestBmiCategory = bmiCat,
                    latestBodyFat = bf,
                    latestMuscleMass = mm
                )
            }
        }

        // Completed sessions and volume bar chart breakdown
        viewModelScope.launch {
            repository.completedSessions.collect { sessions ->
                val inRange = sessions.filter { it.startTime in startTime..endTime }
                val sdf = SimpleDateFormat("MMM d", Locale.getDefault())

                val bars = if (range == TimeRange.ONE_WEEK) {
                    // Daily bars for 7 days
                    (6 downTo 0).map { dayOffset ->
                        val cal = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -dayOffset)
                        }
                        val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
                        val dayStart = cal.apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                        }.timeInMillis
                        val dayEnd = dayStart + (24 * 3600 * 1000)
                        val dayVol = inRange.filter { it.startTime in dayStart until dayEnd }.sumOf { it.totalVolume }
                        ChartBarData(label = dayLabel, value = dayVol)
                    }
                } else {
                    // Weekly buckets
                    inRange.take(8).reversed().map { s ->
                        ChartBarData(label = sdf.format(Date(s.startTime)), value = s.totalVolume)
                    }
                }

                val totalVol = inRange.sumOf { it.totalVolume }
                val totalReps = inRange.sumOf { it.totalReps }
                val totalSets = inRange.sumOf { it.totalSets }

                val avgWeight = if (totalReps > 0) totalVol / totalReps else null
                val avgReps = if (totalSets > 0) totalReps.toDouble() / totalSets else null

                _uiState.value = _uiState.value.copy(
                    recentSessions = inRange,
                    volumeChartData = bars,
                    avgWeightLifted = avgWeight,
                    avgReps = avgReps
                )
            }
        }
    }

    // Body Weight & Composition logging
    fun addBodyWeight(
        weight: Double,
        bodyFat: Double? = null,
        muscleMass: Double? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            repository.addBodyWeightEntry(
                weight = weight,
                unit = _uiState.value.preferredUnit,
                bodyFatPercentage = bodyFat,
                muscleMass = muscleMass,
                date = System.currentTimeMillis(),
                notes = notes
            )
        }
    }

    fun deleteBodyWeight(id: Long) {
        viewModelScope.launch {
            repository.deleteBodyWeightEntry(id)
        }
    }

    // Reports calculations
    fun setReportMonth(month: Int) {
        _reportsState.value = _reportsState.value.copy(selectedMonth = month)
        loadReports()
    }

    fun setReportYear(year: Int) {
        _reportsState.value = _reportsState.value.copy(selectedYear = year)
        loadReports()
    }

    private fun loadReports() {
        val year = _reportsState.value.selectedYear
        val month = _reportsState.value.selectedMonth

        val calCurrent = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val currentStart = calCurrent.timeInMillis
        val currentEnd = calCurrent.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        val calPrev = Calendar.getInstance().apply {
            timeInMillis = currentStart
            add(Calendar.MONTH, -1)
        }
        val prevStart = calPrev.timeInMillis
        val prevEnd = calPrev.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        viewModelScope.launch {
            repository.getProgressSummary(currentStart, currentEnd).collect { curr ->
                repository.getProgressSummary(prevStart, prevEnd).collect { prev ->
                    val deltaPercent = if (prev.totalVolume > 0) {
                        ((curr.totalVolume - prev.totalVolume) / prev.totalVolume) * 100
                    } else null

                    _reportsState.value = _reportsState.value.copy(
                        currentMonthSummary = curr,
                        previousMonthSummary = prev,
                        volumeDeltaPercent = deltaPercent
                    )
                }
            }
        }
    }
}
