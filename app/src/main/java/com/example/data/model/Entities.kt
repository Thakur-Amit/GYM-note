package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Calendar
import kotlin.math.roundToInt

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Athlete",
    val profilePicturePath: String? = null,
    val dateOfBirth: Long? = null, // Epoch millis
    val heightCm: Double? = 175.0,
    val preferredWeightUnit: String = "kg",
    val gender: String = "male", // "male" or "female" for U.S. Navy formula
    val measurementReminderWeeks: Int = 2,
    val fitnessJourneyStartDate: Long? = System.currentTimeMillis(),
    val fitnessGoal: String? = "Build Strength & Lean Muscle",
    val personalNotes: String? = null
) {
    fun calculateAge(currentEpochMillis: Long = System.currentTimeMillis()): Int? {
        val dob = dateOfBirth ?: return null
        val dobCalendar = Calendar.getInstance().apply { timeInMillis = dob }
        val nowCalendar = Calendar.getInstance().apply { timeInMillis = currentEpochMillis }
        
        var age = nowCalendar.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
        if (nowCalendar.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return if (age >= 0) age else 0
    }
}

@Entity(
    tableName = "body_weight_entries",
    indices = [Index(value = ["date"])]
)
data class BodyWeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weight: Double, // Value in preferred unit at entry time
    val weightUnit: String = "kg",
    val bodyFatPercentage: Double? = null, // e.g. 15.2 (%)
    val muscleMass: Double? = null, // Lean muscle mass in preferred unit
    val date: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val weightInKg: Double
        get() = if (weightUnit.equals("lb", ignoreCase = true)) weight * 0.45359237 else weight

    fun calculateBmi(heightCm: Double?): Double? {
        if (heightCm == null || heightCm <= 0.0) return null
        val heightM = heightCm / 100.0
        val bmi = weightInKg / (heightM * heightM)
        return (bmi * 10.0).roundToInt() / 10.0
    }

    fun getBmiCategory(heightCm: Double?): String? {
        val bmi = calculateBmi(heightCm) ?: return null
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    fun calculateBodyFatPercentage(heightCm: Double?, age: Int?): Double? {
        if (bodyFatPercentage != null && bodyFatPercentage > 0.0) {
            return (bodyFatPercentage * 10.0).roundToInt() / 10.0
        }
        val bmi = calculateBmi(heightCm) ?: return null
        val userAge = age ?: 25
        // Deurenberg clinical formula: Body Fat % = 1.20 * BMI + 0.23 * Age - 16.2
        val est = (1.20 * bmi) + (0.23 * userAge) - 16.2
        return ((est.coerceIn(5.0, 50.0)) * 10.0).roundToInt() / 10.0
    }

    fun calculateMuscleMass(heightCm: Double?, age: Int?): Double? {
        if (muscleMass != null && muscleMass > 0.0) {
            return (muscleMass * 10.0).roundToInt() / 10.0
        }
        val bf = calculateBodyFatPercentage(heightCm, age) ?: return null
        val mass = weight * (1.0 - (bf / 100.0))
        return (mass * 10.0).roundToInt() / 10.0
    }
}

@Entity(
    tableName = "body_measurement_entries",
    indices = [Index(value = ["measuredAt"])]
)
data class BodyMeasurementEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val measuredAt: Long = System.currentTimeMillis(),
    val waistCm: Double? = null,
    val neckCm: Double? = null,
    val hipsCm: Double? = null,
    val chestCm: Double? = null,
    val shouldersCm: Double? = null,
    val leftArmCm: Double? = null,
    val rightArmCm: Double? = null,
    val leftForearmCm: Double? = null,
    val rightForearmCm: Double? = null,
    val leftThighCm: Double? = null,
    val rightThighCm: Double? = null,
    val leftCalfCm: Double? = null,
    val rightCalfCm: Double? = null,
    val unit: String = "cm",
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "body_composition_snapshots",
    indices = [Index(value = ["measuredAt"])]
)
data class BodyCompositionSnapshot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val measuredAt: Long = System.currentTimeMillis(),
    val weightKgSnapshot: Double,
    val heightCmSnapshot: Double,
    val formulaInputSex: String = "male",
    val waistCmSnapshot: Double? = null,
    val neckCmSnapshot: Double? = null,
    val hipsCmSnapshot: Double? = null,
    val bmiValue: Double,
    val bodyFatPercentEstimate: Double? = null,
    val fatFreeMassKgEstimate: Double? = null,
    val formulaName: String = "U.S. Navy Circumference",
    val formulaVersion: String = "1.0",
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "exercises",
    indices = [Index(value = ["primaryMuscleGroup"]), Index(value = ["isArchived"])]
)
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val primaryMuscleGroup: String, // from MuscleGroup.name
    val secondaryMuscleGroups: String = "",
    val exerciseType: String = ExerciseType.BARBELL.name,
    val equipment: String = "Barbell",
    val instructions: String? = null,
    val imagePath: String? = null, // Internal file path or "res:drawable_name"
    val loadEntryMode: String = LoadEntryMode.WEIGHT_AND_REPS.name,
    val notes: String? = null,
    val isCustom: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "workout_templates")
data class WorkoutTemplate(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
    val dayOfWeek: String? = null,
    val isCustom: Boolean = false,
    val isArchived: Boolean = false,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "template_exercises",
    indices = [Index(value = ["templateId"])]
)
data class TemplateExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: String,
    val exerciseId: String,
    val orderIndex: Int = 0,
    val targetSets: Int = 3,
    val targetRepRange: String = "8-12",
    val targetWeight: Double? = null,
    val notes: String? = null
)

@Entity(tableName = "workout_schedules")
data class WorkoutSchedule(
    @PrimaryKey val id: String = "active_schedule",
    val cycleType: String = "WEEKLY", // "WEEKLY" or "CUSTOM_CYCLE"
    val cycleLength: Int = 7,
    val cycleStartDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "schedule_days",
    indices = [Index(value = ["scheduleId"]), Index(value = ["dayIndex"])]
)
data class ScheduleDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scheduleId: String = "active_schedule",
    val dayIndex: Int, // 0..6 for Mon..Sun
    val dayName: String,
    val isRestDay: Boolean = false,
    val templateId: String? = null
)

@Entity(
    tableName = "workout_sessions",
    indices = [Index(value = ["startTime"]), Index(value = ["isCompleted"])]
)
data class WorkoutSession(
    @PrimaryKey val id: String,
    val name: String,
    val templateId: String? = null,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val isCompleted: Boolean = false,
    val notes: String? = null,
    val totalVolume: Double = 0.0,
    val totalSets: Int = 0,
    val totalReps: Int = 0,
    val durationSeconds: Long = 0L
)

@Entity(
    tableName = "session_exercises",
    indices = [Index(value = ["sessionId"]), Index(value = ["exerciseId"])]
)
data class SessionExercise(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseId: String,
    val exerciseName: String,
    val primaryMuscleGroup: String,
    val imagePath: String? = null,
    val orderIndex: Int = 0,
    val notes: String? = null,
    val isSkipped: Boolean = false
)

@Entity(
    tableName = "workout_sets",
    indices = [Index(value = ["sessionExerciseId"]), Index(value = ["sessionId"])]
)
data class WorkoutSet(
    @PrimaryKey val id: String,
    val sessionExerciseId: String,
    val sessionId: String,
    val setNumber: Int,
    val setTag: String = SetTag.NORMAL.name,
    val weight: Double? = null,
    val reps: Int? = null,
    val weightUnit: String = "kg",
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val rpe: Double? = null
) {
    // Set volume calculation (weight * reps for completed sets with positive load)
    val volume: Double
        get() {
            if (!isCompleted) return 0.0
            val w = weight ?: 0.0
            val r = reps ?: 0
            return if (w > 0 && r > 0) w * r else 0.0
        }

    // Estimated 1RM using Epley formula: weight * (1 + reps / 30)
    val estimated1RM: Double?
        get() {
            val w = weight ?: return null
            val r = reps ?: return null
            if (w <= 0 || r <= 0) return null
            return if (r == 1) w else w * (1.0 + r / 30.0)
        }
}

data class QuickSetInput(
    val setNumber: Int,
    val weight: Double?,
    val reps: Int?,
    val weightUnit: String = "kg",
    val isCompleted: Boolean = true
)
