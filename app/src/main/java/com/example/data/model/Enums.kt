package com.example.data.model

enum class MuscleGroup(val displayName: String) {
    CHEST("Chest"),
    BACK("Back"),
    SHOULDERS("Shoulders"),
    BICEPS("Biceps"),
    TRICEPS("Triceps"),
    FOREARMS("Forearms"),
    LEGS("Legs"),
    GLUTES("Glutes"),
    CALVES("Calves"),
    CORE_ABS("Core/Abs"),
    CARDIO("Cardio"),
    FULL_BODY("Full Body"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): MuscleGroup {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: OTHER
        }
    }
}

enum class ExerciseType(val displayName: String) {
    BARBELL("Barbell"),
    DUMBBELL("Dumbbell"),
    MACHINE("Machine"),
    CABLE("Cable"),
    BODYWEIGHT("Bodyweight"),
    WEIGHTED_BODYWEIGHT("Weighted Bodyweight"),
    CARDIO("Cardio"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): ExerciseType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: OTHER
        }
    }
}

enum class LoadEntryMode(val displayName: String) {
    WEIGHT_AND_REPS("Weight & Reps"),
    REPS_ONLY("Reps Only"),
    DURATION("Duration"),
    BODYWEIGHT_PLUS_WEIGHT("Bodyweight + Weight");

    companion object {
        fun fromString(value: String): LoadEntryMode {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
                ?: WEIGHT_AND_REPS
        }
    }
}

enum class WeightUnit(val symbol: String, val multiplierToKg: Double) {
    KG("kg", 1.0),
    LB("lb", 0.45359237);

    fun convertTo(target: WeightUnit, value: Double): Double {
        if (this == target) return value
        return if (this == KG && target == LB) {
            value * 2.20462262
        } else {
            value * 0.45359237
        }
    }

    companion object {
        fun fromString(value: String?): WeightUnit {
            return when (value?.lowercase()) {
                "lb", "lbs", "pounds" -> LB
                else -> KG
            }
        }
    }
}

enum class SetTag(val label: String) {
    NORMAL("Normal"),
    WARMUP("Warm-up"),
    DROPSET("Drop Set"),
    FAILURE("Failure")
}
