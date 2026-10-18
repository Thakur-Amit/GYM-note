package com.example.data.model

data class SessionExerciseWithSets(
    val exercise: SessionExercise,
    val sets: List<WorkoutSet>
) {
    val completedSetsCount: Int
        get() = sets.count { it.isCompleted }

    val exerciseVolume: Double
        get() = sets.filter { it.isCompleted }.sumOf { it.volume }

    val maxWeight: Double?
        get() = sets.filter { it.isCompleted }.mapNotNull { it.weight }.maxOrNull()

    val bestEstimated1RM: Double?
        get() = sets.filter { it.isCompleted }.mapNotNull { it.estimated1RM }.maxOrNull()
}

data class WorkoutSessionWithDetails(
    val session: WorkoutSession,
    val exercises: List<SessionExerciseWithSets>
) {
    val completedExercisesCount: Int
        get() = exercises.count { !it.exercise.isSkipped && it.completedSetsCount > 0 }

    val totalCompletedSets: Int
        get() = exercises.sumOf { it.completedSetsCount }

    val computedTotalVolume: Double
        get() = exercises.sumOf { it.exerciseVolume }

    val computedTotalReps: Int
        get() = exercises.sumOf { ex -> ex.sets.filter { it.isCompleted }.sumOf { it.reps ?: 0 } }
}

data class TemplateExerciseDetail(
    val templateExercise: TemplateExercise,
    val exercise: Exercise?
)

data class WorkoutTemplateWithExercises(
    val template: WorkoutTemplate,
    val exercises: List<TemplateExerciseDetail>
)

data class ScheduleDayDetail(
    val scheduleDay: ScheduleDay,
    val template: WorkoutTemplate?
)

data class ExercisePerformanceRecord(
    val date: Long,
    val sessionId: String,
    val sets: List<WorkoutSet>,
    val totalVolume: Double,
    val maxWeight: Double,
    val estimated1RM: Double?
)

data class ExercisePRSummary(
    val maxWeight: Double? = null,
    val maxVolumeSession: Double? = null,
    val bestEstimated1RM: Double? = null,
    val totalWorkoutsCount: Int = 0
)

data class ProgressSummary(
    val completedWorkoutsCount: Int,
    val completedSetsCount: Int,
    val totalReps: Int,
    val totalVolume: Double,
    val consistencyRatePercent: Int,
    val currentWeight: Double?,
    val weightChange: Double?,
    val muscleGroupVolumeMap: Map<String, Double>
)
