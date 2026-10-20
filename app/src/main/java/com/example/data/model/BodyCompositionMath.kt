package com.example.data.model

import kotlin.math.log10
import kotlin.math.roundToInt

object BodyCompositionMath {

    /**
     * Calculates Body Mass Index (BMI).
     * Formula: weight (kg) / (height (m) ^ 2)
     */
    fun calculateBmi(weightKg: Double, heightCm: Double?): Double? {
        if (heightCm == null || heightCm <= 0.0 || weightKg <= 0.0) return null
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        if (bmi.isNaN() || bmi.isInfinite() || bmi <= 0.0) return null
        return (bmi * 10.0).roundToInt() / 10.0
    }

    /**
     * Returns clinical adult BMI category for individuals 20+ years of age.
     * For individuals under 20, provides general guidance without clinical adult diagnosis.
     */
    fun getBmiCategory(bmi: Double?, age: Int? = null): String {
        if (bmi == null) return "Unknown"
        if (age != null && age < 20) {
            return "Pediatric / Youth ($bmi)"
        }
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Healthy Weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obesity"
        }
    }

    /**
     * Calculates Estimated Body Fat % using the official U.S. Navy Circumference Method.
     * All measurements must be in centimeters.
     * 
     * Males:
     *   %Fat = 86.010 * log10(waist - neck) - 70.041 * log10(height) + 36.76
     * 
     * Females:
     *   %Fat = 163.205 * log10(waist + hips - neck) - 97.684 * log10(height) - 78.387
     */
    fun calculateNavyBodyFat(
        gender: String,
        heightCm: Double?,
        waistCm: Double?,
        neckCm: Double?,
        hipsCm: Double? = null
    ): Double? {
        if (heightCm == null || heightCm <= 0.0 || waistCm == null || waistCm <= 0.0 || neckCm == null || neckCm <= 0.0) {
            return null
        }

        val isFemale = gender.equals("female", ignoreCase = true) || gender.equals("f", ignoreCase = true)

        if (isFemale) {
            if (hipsCm == null || hipsCm <= 0.0) return null
            val waistHipsMinusNeck = waistCm + hipsCm - neckCm
            if (waistHipsMinusNeck <= 0.0) return null
            val bodyFat = 163.205 * log10(waistHipsMinusNeck) - 97.684 * log10(heightCm) - 78.387
            if (bodyFat.isNaN() || bodyFat.isInfinite()) return null
            val clamped = bodyFat.coerceIn(3.0, 65.0)
            return (clamped * 10.0).roundToInt() / 10.0
        } else {
            val waistMinusNeck = waistCm - neckCm
            if (waistMinusNeck <= 0.0) return null
            val bodyFat = 86.010 * log10(waistMinusNeck) - 70.041 * log10(heightCm) + 36.76
            if (bodyFat.isNaN() || bodyFat.isInfinite()) return null
            val clamped = bodyFat.coerceIn(2.0, 60.0)
            return (clamped * 10.0).roundToInt() / 10.0
        }
    }

    /**
     * Calculates Estimated Fat-Free Mass (Lean Body Mass) in kg.
     * Formula: weightKg * (1 - (bodyFatPercentage / 100))
     */
    fun calculateFatFreeMass(weightKg: Double, bodyFatPercentage: Double?): Double? {
        if (weightKg <= 0.0 || bodyFatPercentage == null) return null
        val ffm = weightKg * (1.0 - (bodyFatPercentage / 100.0))
        if (ffm.isNaN() || ffm.isInfinite() || ffm <= 0.0) return null
        return (ffm * 10.0).roundToInt() / 10.0
    }

    /**
     * Clinical Anthropometric estimation (Deurenberg method) based on BMI, age, and sex.
     * Sex: 1 for male, 0 for female.
     */
    fun calculateClinicalBodyFat(bmi: Double?, age: Int?, gender: String = "male"): Double? {
        if (bmi == null || age == null || age <= 0) return null
        val sexFactor = if (gender.equals("female", ignoreCase = true)) 0.0 else 1.0
        val bf = (1.20 * bmi) + (0.23 * age) - (10.8 * sexFactor) - 5.4
        val clamped = bf.coerceIn(3.0, 65.0)
        return (clamped * 10.0).roundToInt() / 10.0
    }
}
