package com.example.util

import java.util.Calendar
import kotlin.math.log10
import kotlin.math.roundToInt

object BodyMath {

    /**
     * Epley Formula for Estimated 1RM:
     * 1RM = weight * (1 + reps / 30.0)
     *
     * Rules:
     * - If reps == 1, 1RM equals exact lifted weight.
     * - If weight <= 0 or reps <= 0, returns 0.0.
     * - Bodyweight-only exercises (weight == 0.0) return 0.0 (reps tracked separately).
     */
    fun calculateEpley1RM(weight: Double, reps: Int): Double {
        if (weight <= 0.0 || reps <= 0) return 0.0
        if (reps == 1) return weight
        return weight * (1.0 + (reps.toDouble() / 30.0))
    }

    /**
     * Calculates Body Mass Index:
     * BMI = weightInKg / (heightInMeters ^ 2)
     */
    fun calculateBmi(weightKg: Double, heightCm: Double): Double? {
        if (weightKg <= 0.0 || heightCm <= 0.0) return null
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        return (bmi * 10.0).roundToInt() / 10.0
    }

    /**
     * Returns conventional WHO adult BMI category strictly when age >= 20.
     * For users under 20, returns null (adult categories do not apply).
     */
    fun getBmiCategory(bmi: Double, ageYears: Int?): String? {
        if (ageYears == null || ageYears < 20) return null
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Healthy weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obesity range"
        }
    }

    /**
     * Calculates Estimated Body Fat % using the standard U.S. Navy circumference method.
     *
     * Standard Navy equations use inches and log10:
     * inches = centimeters / 2.54
     *
     * Men:
     * % Fat = 86.010 * log10(waist - neck) - 70.041 * log10(height) + 36.76
     * (Requires waist > neck)
     *
     * Women:
     * % Fat = 163.205 * log10(waist + hip - neck) - 97.684 * log10(height) - 78.387
     * (Requires waist + hip > neck)
     *
     * Returns null if any required input is missing, non-positive, or if logarithm argument is invalid.
     */
    fun calculateNavyBodyFatPercentage(
        heightCm: Double,
        waistCm: Double,
        neckCm: Double,
        hipCm: Double?,
        sex: String
    ): Double? {
        if (heightCm <= 0.0 || waistCm <= 0.0 || neckCm <= 0.0) return null

        val heightIn = heightCm / 2.54
        val waistIn = waistCm / 2.54
        val neckIn = neckCm / 2.54

        val isFemale = sex.trim().equals("female", ignoreCase = true) || sex.trim().equals("f", ignoreCase = true)

        if (isFemale) {
            if (hipCm == null || hipCm <= 0.0) return null
            val hipIn = hipCm / 2.54
            val arg = waistIn + hipIn - neckIn
            if (arg <= 0.0 || heightIn <= 0.0) return null

            val bf = 163.205 * log10(arg) - 97.684 * log10(heightIn) - 78.387
            return if (bf.isFinite() && bf in 3.0..65.0) {
                (bf * 10.0).roundToInt() / 10.0
            } else null
        } else {
            val arg = waistIn - neckIn
            if (arg <= 0.0 || heightIn <= 0.0) return null

            val bf = 86.010 * log10(arg) - 70.041 * log10(heightIn) + 36.76
            return if (bf.isFinite() && bf in 3.0..60.0) {
                (bf * 10.0).roundToInt() / 10.0
            } else null
        }
    }

    /**
     * Calculates Estimated Fat-Free Mass (kg):
     * Fat-Free Mass = weightKg * (1 - (bodyFatPercentage / 100))
     *
     * Note: This is Estimated Fat-Free Mass, NOT skeletal muscle mass.
     */
    fun calculateFatFreeMass(weightKg: Double, bodyFatPercentage: Double): Double? {
        if (weightKg <= 0.0 || bodyFatPercentage <= 0.0 || bodyFatPercentage >= 100.0) return null
        val ffm = weightKg * (1.0 - (bodyFatPercentage / 100.0))
        return (ffm * 10.0).roundToInt() / 10.0
    }

    /**
     * Converts Android Calendar.DAY_OF_WEEK (Sunday = 1, Monday = 2, ..., Saturday = 7)
     * to the application's database day index (Sunday = 0, Monday = 1, ..., Saturday = 6).
     */
    fun getDatabaseDayIndex(calendar: Calendar = Calendar.getInstance()): Int {
        val calendarDay = calendar.get(Calendar.DAY_OF_WEEK)
        return (calendarDay - 1).coerceIn(0, 6)
    }

    /**
     * Returns full day label from database day index (0 = Sunday .. 6 = Saturday)
     */
    fun getDayName(dbDayIndex: Int): String {
        return when (dbDayIndex) {
            0 -> "Sunday"
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            else -> "Day"
        }
    }
}
