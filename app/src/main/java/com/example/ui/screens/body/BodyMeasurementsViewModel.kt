package com.example.ui.screens.body

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BodyCompositionSnapshot
import com.example.data.model.BodyMeasurementEntry
import com.example.data.model.BodyWeightEntry
import com.example.data.model.UserProfile
import com.example.data.repository.GymRepository
import com.example.util.BodyMath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Locale

data class MetricRecord(
    val dateMillis: Long,
    val value: Double
)

data class MeasurementProgressItem(
    val id: String,
    val name: String,
    val currentFormatted: String,
    val changeFormatted: String,
    val isDecreased: Boolean?,
    val historyPoints: List<Double>
)

data class MeasurementSummaryDelta(
    val currentVal: Double?,
    val previousVal: Double?,
    val unit: String = "cm"
) {
    val delta: Double? = if (currentVal != null && previousVal != null) currentVal - previousVal else null
    val deltaFormatted: String? = delta?.let {
        val sign = if (it > 0) "+" else ""
        val arrow = if (it < 0) "↓ " else if (it > 0) "↑ " else ""
        "$arrow$sign${String.format(Locale.getDefault(), "%.1f", it)} $unit"
    }
    val deltaSimple: String? = delta?.let {
        val sign = if (it > 0) "+" else ""
        val arrow = if (it < 0) "↓ " else if (it > 0) "↑ " else ""
        "$arrow$sign${String.format(Locale.getDefault(), "%.1f", it)}"
    }
    val isDecreased: Boolean? = delta?.let { it < 0 }
}

data class BodyMeasurementsUiState(
    val userProfile: UserProfile = UserProfile(),
    val latestMeasurement: BodyMeasurementEntry? = null,
    val previousMeasurement: BodyMeasurementEntry? = null,
    val allMeasurements: List<BodyMeasurementEntry> = emptyList(),
    val latestWeight: BodyWeightEntry? = null,
    val previousWeight: BodyWeightEntry? = null,
    val allWeights: List<BodyWeightEntry> = emptyList(),
    val latestComposition: BodyCompositionSnapshot? = null,
    val previousComposition: BodyCompositionSnapshot? = null,
    val allCompositions: List<BodyCompositionSnapshot> = emptyList(),

    // Summary KPI deltas
    val weightDelta: MeasurementSummaryDelta = MeasurementSummaryDelta(null, null, "kg"),
    val bodyFatDelta: MeasurementSummaryDelta = MeasurementSummaryDelta(null, null, "%"),
    val bmiDelta: MeasurementSummaryDelta = MeasurementSummaryDelta(null, null, ""),

    // Body Figure Deltas
    val neckDelta: MeasurementSummaryDelta = MeasurementSummaryDelta(null, null),
    val chestDelta: MeasurementSummaryDelta = MeasurementSummaryDelta(null, null),
    val leftArmDelta: MeasurementSummaryDelta = MeasurementSummaryDelta(null, null),
    val rightArmDelta: MeasurementSummaryDelta = MeasurementSummaryDelta(null, null),
    val waistDelta: MeasurementSummaryDelta = MeasurementSummaryDelta(null, null),
    val hipsDelta: MeasurementSummaryDelta = MeasurementSummaryDelta(null, null),
    val shouldersDelta: MeasurementSummaryDelta = MeasurementSummaryDelta(null, null),
    val leftThighDelta: MeasurementSummaryDelta = MeasurementSummaryDelta(null, null),
    val rightThighDelta: MeasurementSummaryDelta = MeasurementSummaryDelta(null, null),
    val leftCalfDelta: MeasurementSummaryDelta = MeasurementSummaryDelta(null, null),
    val rightCalfDelta: MeasurementSummaryDelta = MeasurementSummaryDelta(null, null),

    // Charts Tab State
    val selectedChartMetric: String = "Weight",
    val selectedTimeRange: String = "3M", // 1M, 3M, 6M, 1Y, All
    val chartPoints: List<MetricRecord> = emptyList(),
    val chartTotalChange: String = "",
    val chartIsDecreased: Boolean? = null,
    val progressItems: List<MeasurementProgressItem> = emptyList(),

    val isLoading: Boolean = false
)

class BodyMeasurementsViewModel(
    private val repository: GymRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BodyMeasurementsUiState())
    val uiState: StateFlow<BodyMeasurementsUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.userProfile,
                repository.allBodyMeasurements,
                repository.allBodyWeightEntries,
                repository.allCompositionSnapshots
            ) { profile, measurements, weights, compositions ->
                val prof = profile ?: UserProfile()
                val latestM = measurements.getOrNull(0)
                val prevM = measurements.getOrNull(1)

                val latestW = weights.getOrNull(0)
                val prevW = weights.getOrNull(1)

                val latestC = compositions.getOrNull(0)
                val prevC = compositions.getOrNull(1)

                // KPI Deltas
                val wDelta = MeasurementSummaryDelta(latestW?.weight, prevW?.weight, "kg")

                // Body Fat %: from snapshot or computed
                val currentBf = latestC?.bodyFatPercentEstimate ?: latestW?.bodyFatPercentage
                val prevBf = prevC?.bodyFatPercentEstimate ?: prevW?.bodyFatPercentage
                val bfDelta = MeasurementSummaryDelta(currentBf, prevBf, "%")

                // BMI: from snapshot or computed
                val currentBmi = latestC?.bmiValue ?: latestW?.calculateBmi(prof.heightCm)
                val prevBmi = prevC?.bmiValue ?: prevW?.calculateBmi(prof.heightCm)
                val bDelta = MeasurementSummaryDelta(currentBmi, prevBmi, "")

                // Circumference deltas
                val neck = MeasurementSummaryDelta(latestM?.neckCm, prevM?.neckCm)
                val chest = MeasurementSummaryDelta(latestM?.chestCm, prevM?.chestCm)
                val lArm = MeasurementSummaryDelta(latestM?.leftArmCm, prevM?.leftArmCm)
                val rArm = MeasurementSummaryDelta(latestM?.rightArmCm, prevM?.rightArmCm)
                val waist = MeasurementSummaryDelta(latestM?.waistCm, prevM?.waistCm)
                val hips = MeasurementSummaryDelta(latestM?.hipsCm, prevM?.hipsCm)
                val shoulders = MeasurementSummaryDelta(latestM?.shouldersCm, prevM?.shouldersCm)
                val lThigh = MeasurementSummaryDelta(latestM?.leftThighCm, prevM?.leftThighCm)
                val rThigh = MeasurementSummaryDelta(latestM?.rightThighCm, prevM?.rightThighCm)
                val lCalf = MeasurementSummaryDelta(latestM?.leftCalfCm, prevM?.leftCalfCm)
                val rCalf = MeasurementSummaryDelta(latestM?.rightCalfCm, prevM?.rightCalfCm)

                _uiState.value.copy(
                    userProfile = prof,
                    latestMeasurement = latestM,
                    previousMeasurement = prevM,
                    allMeasurements = measurements,
                    latestWeight = latestW,
                    previousWeight = prevW,
                    allWeights = weights,
                    latestComposition = latestC,
                    previousComposition = prevC,
                    allCompositions = compositions,
                    weightDelta = wDelta,
                    bodyFatDelta = bfDelta,
                    bmiDelta = bDelta,
                    neckDelta = neck,
                    chestDelta = chest,
                    leftArmDelta = lArm,
                    rightArmDelta = rArm,
                    waistDelta = waist,
                    hipsDelta = hips,
                    shouldersDelta = shoulders,
                    leftThighDelta = lThigh,
                    rightThighDelta = rThigh,
                    leftCalfDelta = lCalf,
                    rightCalfDelta = rCalf
                )
            }.collect { updatedState ->
                _uiState.value = updatedState
                refreshChartData()
                refreshProgressItems()
            }
        }
    }

    fun setChartMetric(metric: String) {
        _uiState.value = _uiState.value.copy(selectedChartMetric = metric)
        refreshChartData()
    }

    fun setTimeRange(range: String) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = range)
        refreshChartData()
    }

    private fun refreshChartData() {
        val state = _uiState.value
        val now = System.currentTimeMillis()
        val rangeDays = when (state.selectedTimeRange) {
            "1M" -> 30L
            "3M" -> 90L
            "6M" -> 180L
            "1Y" -> 365L
            else -> 3650L // "All"
        }
        val cutoff = now - (rangeDays * 24L * 3600L * 1000L)

        val points: List<MetricRecord> = when (state.selectedChartMetric) {
            "Weight" -> {
                state.allWeights
                    .filter { it.date >= cutoff }
                    .sortedBy { it.date }
                    .map { MetricRecord(it.date, it.weight) }
            }
            "Body Fat (Est.)", "Body Fat" -> {
                state.allCompositions
                    .filter { it.measuredAt >= cutoff && it.bodyFatPercentEstimate != null }
                    .sortedBy { it.measuredAt }
                    .map { MetricRecord(it.measuredAt, it.bodyFatPercentEstimate!!) }
            }
            "BMI" -> {
                state.allCompositions
                    .filter { it.measuredAt >= cutoff }
                    .sortedBy { it.measuredAt }
                    .map { MetricRecord(it.measuredAt, it.bmiValue) }
            }
            "Neck" -> state.allMeasurements.filter { it.measuredAt >= cutoff && it.neckCm != null }
                .sortedBy { it.measuredAt }.map { MetricRecord(it.measuredAt, it.neckCm!!) }
            "Chest" -> state.allMeasurements.filter { it.measuredAt >= cutoff && it.chestCm != null }
                .sortedBy { it.measuredAt }.map { MetricRecord(it.measuredAt, it.chestCm!!) }
            "Waist" -> state.allMeasurements.filter { it.measuredAt >= cutoff && it.waistCm != null }
                .sortedBy { it.measuredAt }.map { MetricRecord(it.measuredAt, it.waistCm!!) }
            "Hips" -> state.allMeasurements.filter { it.measuredAt >= cutoff && it.hipsCm != null }
                .sortedBy { it.measuredAt }.map { MetricRecord(it.measuredAt, it.hipsCm!!) }
            "Left Arm" -> state.allMeasurements.filter { it.measuredAt >= cutoff && it.leftArmCm != null }
                .sortedBy { it.measuredAt }.map { MetricRecord(it.measuredAt, it.leftArmCm!!) }
            "Right Arm" -> state.allMeasurements.filter { it.measuredAt >= cutoff && it.rightArmCm != null }
                .sortedBy { it.measuredAt }.map { MetricRecord(it.measuredAt, it.rightArmCm!!) }
            "Shoulders" -> state.allMeasurements.filter { it.measuredAt >= cutoff && it.shouldersCm != null }
                .sortedBy { it.measuredAt }.map { MetricRecord(it.measuredAt, it.shouldersCm!!) }
            "Left Thigh" -> state.allMeasurements.filter { it.measuredAt >= cutoff && it.leftThighCm != null }
                .sortedBy { it.measuredAt }.map { MetricRecord(it.measuredAt, it.leftThighCm!!) }
            "Right Thigh" -> state.allMeasurements.filter { it.measuredAt >= cutoff && it.rightThighCm != null }
                .sortedBy { it.measuredAt }.map { MetricRecord(it.measuredAt, it.rightThighCm!!) }
            "Left Calf" -> state.allMeasurements.filter { it.measuredAt >= cutoff && it.leftCalfCm != null }
                .sortedBy { it.measuredAt }.map { MetricRecord(it.measuredAt, it.leftCalfCm!!) }
            "Right Calf" -> state.allMeasurements.filter { it.measuredAt >= cutoff && it.rightCalfCm != null }
                .sortedBy { it.measuredAt }.map { MetricRecord(it.measuredAt, it.rightCalfCm!!) }
            else -> emptyList()
        }

        val totalChangeStr: String
        val isDec: Boolean?
        if (points.size >= 2) {
            val firstVal = points.first().value
            val lastVal = points.last().value
            val diff = lastVal - firstVal
            val sign = if (diff > 0) "+" else ""
            val unit = when (state.selectedChartMetric) {
                "Weight" -> "kg"
                "Body Fat (Est.)", "Body Fat" -> "%"
                "BMI" -> ""
                else -> "cm"
            }
            val arrow = if (diff < 0) "↓ " else if (diff > 0) "↑ " else ""
            totalChangeStr = "$arrow$sign${String.format(Locale.getDefault(), "%.1f", diff)} $unit"
            isDec = diff < 0
        } else {
            totalChangeStr = "No previous change"
            isDec = null
        }

        _uiState.value = _uiState.value.copy(
            chartPoints = points,
            chartTotalChange = totalChangeStr,
            chartIsDecreased = isDec
        )
    }

    private fun refreshProgressItems() {
        val state = _uiState.value
        val items = mutableListOf<MeasurementProgressItem>()

        fun buildItem(
            id: String,
            name: String,
            values: List<Double>,
            unit: String
        ): MeasurementProgressItem? {
            if (values.isEmpty()) return null
            val current = values.first() // newest
            val oldest = values.last()
            val diff = current - oldest
            val sign = if (diff > 0) "+" else ""
            val changeStr = if (values.size > 1) {
                "$sign${String.format(Locale.getDefault(), "%.1f", diff)} $unit"
            } else "—"

            return MeasurementProgressItem(
                id = id,
                name = name,
                currentFormatted = "${String.format(Locale.getDefault(), "%.1f", current)} $unit",
                changeFormatted = changeStr,
                isDecreased = if (values.size > 1) diff < 0 else null,
                historyPoints = values.reversed() // oldest to newest for sparkline
            )
        }

        // Weight
        buildItem("weight", "Weight", state.allWeights.map { it.weight }, "kg")?.let { items.add(it) }

        // Body Fat %
        val bfValues = state.allCompositions.mapNotNull { it.bodyFatPercentEstimate }
            .ifEmpty { state.allWeights.mapNotNull { it.bodyFatPercentage } }
        buildItem("body_fat", "Body Fat (Est.)", bfValues, "%")?.let { items.add(it) }

        // BMI
        val bmiValues = state.allCompositions.map { it.bmiValue }
            .ifEmpty { state.allWeights.mapNotNull { it.calculateBmi(state.userProfile.heightCm) } }
        buildItem("bmi", "BMI", bmiValues, "")?.let { items.add(it) }

        // Circumferences
        buildItem("neck", "Neck", state.allMeasurements.mapNotNull { it.neckCm }, "cm")?.let { items.add(it) }
        buildItem("chest", "Chest", state.allMeasurements.mapNotNull { it.chestCm }, "cm")?.let { items.add(it) }
        buildItem("waist", "Waist", state.allMeasurements.mapNotNull { it.waistCm }, "cm")?.let { items.add(it) }
        buildItem("hips", "Hips", state.allMeasurements.mapNotNull { it.hipsCm }, "cm")?.let { items.add(it) }
        buildItem("left_arm", "Left Arm", state.allMeasurements.mapNotNull { it.leftArmCm }, "cm")?.let { items.add(it) }
        buildItem("right_arm", "Right Arm", state.allMeasurements.mapNotNull { it.rightArmCm }, "cm")?.let { items.add(it) }
        buildItem("shoulders", "Shoulders", state.allMeasurements.mapNotNull { it.shouldersCm }, "cm")?.let { items.add(it) }
        buildItem("left_thigh", "Left Thigh", state.allMeasurements.mapNotNull { it.leftThighCm }, "cm")?.let { items.add(it) }
        buildItem("right_thigh", "Right Thigh", state.allMeasurements.mapNotNull { it.rightThighCm }, "cm")?.let { items.add(it) }
        buildItem("left_calf", "Left Calf", state.allMeasurements.mapNotNull { it.leftCalfCm }, "cm")?.let { items.add(it) }
        buildItem("right_calf", "Right Calf", state.allMeasurements.mapNotNull { it.rightCalfCm }, "cm")?.let { items.add(it) }

        _uiState.value = _uiState.value.copy(progressItems = items)
    }

    fun saveNewMeasurement(
        weightKg: Double?,
        measuredAt: Long,
        waistCm: Double?,
        neckCm: Double?,
        hipsCm: Double?,
        chestCm: Double?,
        shouldersCm: Double?,
        leftArmCm: Double?,
        rightArmCm: Double?,
        leftForearmCm: Double?,
        rightForearmCm: Double?,
        leftThighCm: Double?,
        rightThighCm: Double?,
        leftCalfCm: Double?,
        rightCalfCm: Double?,
        notes: String?,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val now = measuredAt
            val profile = _uiState.value.userProfile

            // 1. Calculate U.S. Navy Body Fat % & BMI if inputs present
            val heightCm = profile.heightCm ?: 175.0
            val sex = profile.gender

            val calculatedBf = if (waistCm != null && neckCm != null) {
                BodyMath.calculateNavyBodyFatPercentage(
                    heightCm = heightCm,
                    waistCm = waistCm,
                    neckCm = neckCm,
                    hipCm = hipsCm,
                    sex = sex
                )
            } else null

            val calculatedBmi = if (weightKg != null) {
                BodyMath.calculateBmi(weightKg, heightCm)
            } else null

            val calculatedFfm = if (weightKg != null && calculatedBf != null) {
                BodyMath.calculateFatFreeMass(weightKg, calculatedBf)
            } else null

            // 2. Insert BodyWeightEntry
            if (weightKg != null && weightKg > 0.0) {
                repository.addBodyWeightEntry(
                    weight = weightKg,
                    unit = "kg",
                    bodyFatPercentage = calculatedBf,
                    muscleMass = calculatedFfm,
                    date = now,
                    notes = notes
                )
            }

            // 3. Insert BodyMeasurementEntry
            val hasCircumference = waistCm != null || neckCm != null || hipsCm != null ||
                    chestCm != null || shouldersCm != null || leftArmCm != null || rightArmCm != null ||
                    leftThighCm != null || rightThighCm != null || leftCalfCm != null || rightCalfCm != null

            if (hasCircumference) {
                repository.addBodyMeasurementEntry(
                    BodyMeasurementEntry(
                        measuredAt = now,
                        waistCm = waistCm,
                        neckCm = neckCm,
                        hipsCm = hipsCm,
                        chestCm = chestCm,
                        shouldersCm = shouldersCm,
                        leftArmCm = leftArmCm,
                        rightArmCm = rightArmCm,
                        leftForearmCm = leftForearmCm,
                        rightForearmCm = rightForearmCm,
                        leftThighCm = leftThighCm,
                        rightThighCm = rightThighCm,
                        leftCalfCm = leftCalfCm,
                        rightCalfCm = rightCalfCm,
                        unit = "cm",
                        notes = notes,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }

            // 4. Insert BodyCompositionSnapshot
            if (weightKg != null && calculatedBmi != null) {
                repository.addCompositionSnapshot(
                    BodyCompositionSnapshot(
                        measuredAt = now,
                        weightKgSnapshot = weightKg,
                        heightCmSnapshot = heightCm,
                        formulaInputSex = sex,
                        waistCmSnapshot = waistCm,
                        neckCmSnapshot = neckCm,
                        hipsCmSnapshot = hipsCm,
                        bmiValue = calculatedBmi,
                        bodyFatPercentEstimate = calculatedBf,
                        fatFreeMassKgEstimate = calculatedFfm,
                        formulaName = "U.S. Navy Circumference",
                        formulaVersion = "1.0",
                        notes = notes,
                        createdAt = now
                    )
                )
            }

            onSaved()
        }
    }

    fun deleteMeasurement(id: Long) {
        viewModelScope.launch {
            repository.deleteBodyMeasurementEntry(id)
        }
    }
}
