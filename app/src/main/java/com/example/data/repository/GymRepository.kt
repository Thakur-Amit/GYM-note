package com.example.data.repository

import android.net.Uri
import com.example.data.local.AppDatabase
import com.example.data.model.BodyCompositionSnapshot
import com.example.data.model.BodyMeasurementEntry
import com.example.data.model.BodyWeightEntry
import com.example.data.model.Exercise
import com.example.data.model.ExercisePRSummary
import com.example.data.model.ExercisePerformanceRecord
import com.example.data.model.ProgressSummary
import com.example.data.model.ScheduleDay
import com.example.data.model.ScheduleDayDetail
import com.example.data.model.SessionExercise
import com.example.data.model.SessionExerciseWithSets
import com.example.data.model.TemplateExercise
import com.example.data.model.TemplateExerciseDetail
import com.example.data.model.UserProfile
import com.example.data.model.WeightUnit
import com.example.data.model.WorkoutSchedule
import com.example.data.model.WorkoutSession
import com.example.data.model.WorkoutSessionWithDetails
import com.example.data.model.QuickSetInput
import com.example.data.model.WorkoutSet
import com.example.data.model.WorkoutTemplate
import com.example.data.model.WorkoutTemplateWithExercises
import com.example.data.storage.ImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

class GymRepository(
    private val database: AppDatabase,
    private val imageStorageManager: ImageStorageManager
) {
    // ----------------------------------------------------
    // User Profile
    // ----------------------------------------------------
    val userProfile: Flow<UserProfile?> = database.userDao().getUserProfile()

    suspend fun updateProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        database.userDao().insertOrUpdate(profile)
    }

    suspend fun setProfilePicture(uri: Uri): String? = withContext(Dispatchers.IO) {
        val currentProfile = database.userDao().getUserProfileSync()
        val savedPath = imageStorageManager.saveProfilePicture(uri, currentProfile?.profilePicturePath)
        if (savedPath != null) {
            database.userDao().updateProfilePicture(savedPath)
        }
        savedPath
    }

    suspend fun removeProfilePicture() = withContext(Dispatchers.IO) {
        val currentProfile = database.userDao().getUserProfileSync()
        currentProfile?.profilePicturePath?.let {
            imageStorageManager.removeProfilePicture(it)
        }
        database.userDao().updateProfilePicture(null)
    }

    // ----------------------------------------------------
    // Body Weight
    // ----------------------------------------------------
    val allBodyWeightEntries: Flow<List<BodyWeightEntry>> = database.bodyWeightDao().getAllEntries()
    val latestBodyWeight: Flow<BodyWeightEntry?> = database.bodyWeightDao().getLatestEntry()

    suspend fun addBodyWeightEntry(
        weight: Double,
        unit: String,
        bodyFatPercentage: Double? = null,
        muscleMass: Double? = null,
        date: Long = System.currentTimeMillis(),
        notes: String? = null
    ): Long = withContext(Dispatchers.IO) {
        database.bodyWeightDao().insert(
            BodyWeightEntry(
                weight = weight,
                weightUnit = unit,
                bodyFatPercentage = bodyFatPercentage,
                muscleMass = muscleMass,
                date = date,
                notes = notes
            )
        )
    }

    suspend fun deleteBodyWeightEntry(id: Long) = withContext(Dispatchers.IO) {
        database.bodyWeightDao().deleteById(id)
    }

    suspend fun updateBodyWeightEntry(entry: BodyWeightEntry) = withContext(Dispatchers.IO) {
        database.bodyWeightDao().update(entry)
    }

    // ----------------------------------------------------
    // Body Circumference Measurements & Body Composition
    // ----------------------------------------------------
    val allBodyMeasurements: Flow<List<BodyMeasurementEntry>> =
        database.bodyMeasurementDao().getAllEntries()

    val latestBodyMeasurement: Flow<BodyMeasurementEntry?> =
        database.bodyMeasurementDao().getLatestEntry()

    suspend fun addBodyMeasurementEntry(entry: BodyMeasurementEntry): Long = withContext(Dispatchers.IO) {
        database.bodyMeasurementDao().insert(entry)
    }

    suspend fun updateBodyMeasurementEntry(entry: BodyMeasurementEntry) = withContext(Dispatchers.IO) {
        database.bodyMeasurementDao().update(entry)
    }

    suspend fun deleteBodyMeasurementEntry(id: Long) = withContext(Dispatchers.IO) {
        database.bodyMeasurementDao().deleteById(id)
    }

    fun getMeasurementsBetween(start: Long, end: Long): Flow<List<BodyMeasurementEntry>> =
        database.bodyMeasurementDao().getEntriesBetween(start, end)

    val allCompositionSnapshots: Flow<List<BodyCompositionSnapshot>> =
        database.bodyCompositionDao().getAllSnapshots()

    val latestCompositionSnapshot: Flow<BodyCompositionSnapshot?> =
        database.bodyCompositionDao().getLatestSnapshot()

    suspend fun addCompositionSnapshot(snapshot: BodyCompositionSnapshot): Long = withContext(Dispatchers.IO) {
        database.bodyCompositionDao().insert(snapshot)
    }

    suspend fun deleteCompositionSnapshot(id: Long) = withContext(Dispatchers.IO) {
        database.bodyCompositionDao().deleteById(id)
    }

    fun getCompositionSnapshotsBetween(start: Long, end: Long): Flow<List<BodyCompositionSnapshot>> =
        database.bodyCompositionDao().getSnapshotsBetween(start, end)

    // ----------------------------------------------------
    // Exercises
    // ----------------------------------------------------
    val activeExercises: Flow<List<Exercise>> = database.exerciseDao().getAllActiveExercises()

    fun getExerciseById(id: String): Flow<Exercise?> = database.exerciseDao().getExerciseById(id)

    fun getExercisesByMuscleGroup(group: String): Flow<List<Exercise>> =
        database.exerciseDao().getExercisesByMuscleGroup(group)

    fun searchExercises(query: String): Flow<List<Exercise>> =
        database.exerciseDao().searchExercises(query)

    suspend fun saveExercise(exercise: Exercise, imageUri: Uri? = null): Exercise = withContext(Dispatchers.IO) {
        var finalImagePath = exercise.imagePath
        if (imageUri != null) {
            val savedPath = imageStorageManager.saveExerciseImage(imageUri, exercise.id, exercise.imagePath)
            if (savedPath != null) {
                finalImagePath = savedPath
            }
        }
        val toSave = exercise.copy(imagePath = finalImagePath)
        database.exerciseDao().insert(toSave)
        toSave
    }

    suspend fun setExerciseArchived(exerciseId: String, archived: Boolean) = withContext(Dispatchers.IO) {
        database.exerciseDao().setArchived(exerciseId, archived)
    }

    fun getExercisePRSummary(exerciseId: String): Flow<ExercisePRSummary> {
        return database.workoutSessionDao().getCompletedSetsForExerciseHistory(exerciseId).map { sets ->
            if (sets.isEmpty()) {
                ExercisePRSummary()
            } else {
                val maxWeight = sets.mapNotNull { it.weight }.maxOrNull()
                val best1RM = sets.mapNotNull { it.estimated1RM }.maxOrNull()
                val sessionGroups = sets.groupBy { it.sessionId }
                val maxSessionVolume = sessionGroups.values.maxOfOrNull { list -> list.sumOf { it.volume } }
                ExercisePRSummary(
                    maxWeight = maxWeight,
                    maxVolumeSession = maxSessionVolume,
                    bestEstimated1RM = best1RM,
                    totalWorkoutsCount = sessionGroups.size
                )
            }
        }
    }

    fun getExerciseHistory(exerciseId: String): Flow<List<ExercisePerformanceRecord>> {
        return database.workoutSessionDao().getCompletedSetsForExerciseHistory(exerciseId).combine(
            database.workoutSessionDao().getCompletedSessionsForExercise(exerciseId)
        ) { sets, sessions ->
            val sessionMap = sessions.associateBy { it.id }
            val setsBySession = sets.groupBy { it.sessionId }
            setsBySession.mapNotNull { (sessionId, sessionSets) ->
                val session = sessionMap[sessionId] ?: return@mapNotNull null
                val totalVolume = sessionSets.sumOf { it.volume }
                val maxWeight = sessionSets.mapNotNull { it.weight }.maxOrNull() ?: 0.0
                val best1RM = sessionSets.mapNotNull { it.estimated1RM }.maxOrNull()
                ExercisePerformanceRecord(
                    date = session.startTime,
                    sessionId = sessionId,
                    sets = sessionSets,
                    totalVolume = totalVolume,
                    maxWeight = maxWeight,
                    estimated1RM = best1RM
                )
            }.sortedByDescending { it.date }
        }
    }

    fun getPreviousPerformanceForExercise(exerciseId: String): Flow<ExercisePerformanceRecord?> {
        return getExerciseHistory(exerciseId).map { history -> history.firstOrNull() }
    }

    // ----------------------------------------------------
    // Templates
    // ----------------------------------------------------
    val allTemplates: Flow<List<WorkoutTemplate>> = database.templateDao().getAllTemplates()

    fun getTemplateWithExercises(templateId: String): Flow<WorkoutTemplateWithExercises?> {
        return database.templateDao().getTemplateById(templateId).combine(
            database.templateDao().getExercisesForTemplate(templateId)
        ) { template, templateExercises ->
            if (template == null) return@combine null
            val exercisesWithDetails = templateExercises.map { te ->
                val exercise = database.exerciseDao().getExerciseByIdSync(te.exerciseId)
                TemplateExerciseDetail(templateExercise = te, exercise = exercise)
            }
            WorkoutTemplateWithExercises(template = template, exercises = exercisesWithDetails)
        }
    }

    suspend fun saveTemplate(
        template: WorkoutTemplate,
        exercises: List<TemplateExercise>
    ) = withContext(Dispatchers.IO) {
        database.templateDao().insertTemplate(template)
        database.templateDao().deleteExercisesForTemplate(template.id)
        val withUpdatedTemplateId = exercises.mapIndexed { index, te ->
            te.copy(id = 0, templateId = template.id, orderIndex = index)
        }
        database.templateDao().insertTemplateExercises(withUpdatedTemplateId)
    }

    suspend fun deleteTemplate(templateId: String) = withContext(Dispatchers.IO) {
        val template = database.templateDao().getTemplateByIdSync(templateId)
        if (template != null) {
            database.templateDao().deleteTemplate(template)
            database.templateDao().deleteExercisesForTemplate(templateId)
        }
    }

    suspend fun addExerciseToTemplate(
        templateId: String,
        exerciseId: String,
        targetSets: Int = 3,
        targetReps: String = "8-12",
        targetWeight: Double? = null
    ) = withContext(Dispatchers.IO) {
        val currentExercises = database.templateDao().getExercisesForTemplateSync(templateId)
        val nextOrder = currentExercises.size
        val newTe = TemplateExercise(
            templateId = templateId,
            exerciseId = exerciseId,
            orderIndex = nextOrder,
            targetSets = targetSets,
            targetRepRange = targetReps,
            targetWeight = targetWeight
        )
        database.templateDao().insertTemplateExercise(newTe)
    }

    suspend fun removeExerciseFromTemplate(templateExerciseId: Long) = withContext(Dispatchers.IO) {
        database.templateDao().deleteTemplateExerciseById(templateExerciseId)
    }

    suspend fun duplicateTemplate(templateId: String): String = withContext(Dispatchers.IO) {
        val original = database.templateDao().getTemplateByIdSync(templateId)
            ?: throw IllegalArgumentException("Template not found")
        val newId = "template_${UUID.randomUUID().toString().take(8)}"
        val newTemplate = original.copy(
            id = newId,
            name = "${original.name} (Copy)",
            isCustom = true,
            createdAt = System.currentTimeMillis()
        )
        val exercises = database.templateDao().getExercisesForTemplateSync(templateId)
        val newExercises = exercises.map { it.copy(id = 0, templateId = newId) }
        database.templateDao().insertTemplate(newTemplate)
        database.templateDao().insertTemplateExercises(newExercises)
        newId
    }

    // ----------------------------------------------------
    // Schedule
    // ----------------------------------------------------
    val activeSchedule: Flow<WorkoutSchedule?> = database.scheduleDao().getSchedule()

    val scheduleDays: Flow<List<ScheduleDayDetail>> =
        database.scheduleDao().getScheduleDays().combine(database.templateDao().getAllTemplates()) { days, templates ->
            val templateMap = templates.associateBy { it.id }
            days.map { day ->
                ScheduleDayDetail(
                    scheduleDay = day,
                    template = day.templateId?.let { templateMap[it] }
                )
            }
        }

    fun getTodayScheduledTemplate(): Flow<WorkoutTemplate?> {
        return database.scheduleDao().getScheduleDays().combine(database.templateDao().getAllTemplates()) { days, templates ->
            if (days.isEmpty()) return@combine null
            val calendar = Calendar.getInstance()
            // In Java Calendar, Sunday is 1, Monday is 2 ... Saturday is 7
            // Our 0-indexed Mon-Sun schedule: Monday=0, Tuesday=1 ... Sunday=6
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val scheduleIndex = when (dayOfWeek) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            val todaySchedule = days.firstOrNull { it.dayIndex == scheduleIndex }
            if (todaySchedule == null || todaySchedule.isRestDay || todaySchedule.templateId == null) {
                null
            } else {
                templates.firstOrNull { it.id == todaySchedule.templateId }
            }
        }
    }

    suspend fun updateScheduleDay(day: ScheduleDay) = withContext(Dispatchers.IO) {
        database.scheduleDao().updateScheduleDay(day)
    }

    // ----------------------------------------------------
    // Active Workout & Sessions
    // ----------------------------------------------------
    val activeSession: Flow<WorkoutSessionWithDetails?> =
        database.workoutSessionDao().getActiveSession().map { session ->
            if (session == null) return@map null
            val exercises = database.workoutSessionDao().getSessionExercisesSync(session.id)
            val exercisesWithSets = exercises.map { ex ->
                val sets = database.workoutSessionDao().getSetsForExerciseSync(ex.id)
                SessionExerciseWithSets(exercise = ex, sets = sets)
            }
            WorkoutSessionWithDetails(session = session, exercises = exercisesWithSets)
        }.flowOn(Dispatchers.IO)

    val completedSessions: Flow<List<WorkoutSession>> = database.workoutSessionDao().getCompletedSessions()

    fun getSessionWithDetails(sessionId: String): Flow<WorkoutSessionWithDetails?> = flow {
        val session = database.workoutSessionDao().getSessionByIdSync(sessionId)
        if (session == null) {
            emit(null)
            return@flow
        }
        val exercises = database.workoutSessionDao().getSessionExercisesSync(session.id)
        val exercisesWithSets = exercises.map { ex ->
            val sets = database.workoutSessionDao().getSetsForExerciseSync(ex.id)
            SessionExerciseWithSets(exercise = ex, sets = sets)
        }
        emit(WorkoutSessionWithDetails(session = session, exercises = exercisesWithSets))
    }.flowOn(Dispatchers.IO)

    suspend fun startWorkoutFromTemplate(templateId: String): String = withContext(Dispatchers.IO) {
        // Discard any currently pending incomplete session first to keep stable single active state
        val existingActive = database.workoutSessionDao().getActiveSessionSync()
        if (existingActive != null) {
            database.workoutSessionDao().deleteSetsForSession(existingActive.id)
            database.workoutSessionDao().deleteSessionExercisesForSession(existingActive.id)
            database.workoutSessionDao().deleteSession(existingActive.id)
        }

        val template = database.templateDao().getTemplateByIdSync(templateId)
        val sessionId = UUID.randomUUID().toString()
        val sessionName = template?.name ?: "Workout"

        val session = WorkoutSession(
            id = sessionId,
            name = sessionName,
            templateId = templateId,
            startTime = System.currentTimeMillis(),
            isCompleted = false
        )
        database.workoutSessionDao().insertSession(session)

        val templateExercises = database.templateDao().getExercisesForTemplateSync(templateId)
        val profile = database.userDao().getUserProfileSync()
        val preferredUnit = profile?.preferredWeightUnit ?: "kg"

        templateExercises.forEachIndexed { index, te ->
            val exercise = database.exerciseDao().getExerciseByIdSync(te.exerciseId)
            val sessionExId = UUID.randomUUID().toString()
            val sessionExercise = SessionExercise(
                id = sessionExId,
                sessionId = sessionId,
                exerciseId = te.exerciseId,
                exerciseName = exercise?.name ?: "Exercise",
                primaryMuscleGroup = exercise?.primaryMuscleGroup ?: "Other",
                imagePath = exercise?.imagePath,
                orderIndex = index,
                notes = te.notes
            )
            database.workoutSessionDao().insertSessionExercise(sessionExercise)

            // Add target sets
            val numSets = if (te.targetSets > 0) te.targetSets else 3
            val sets = (1..numSets).map { setNum ->
                WorkoutSet(
                    id = UUID.randomUUID().toString(),
                    sessionExerciseId = sessionExId,
                    sessionId = sessionId,
                    setNumber = setNum,
                    weight = te.targetWeight,
                    reps = null,
                    weightUnit = preferredUnit,
                    isCompleted = false
                )
            }
            database.workoutSessionDao().insertSets(sets)
        }

        sessionId
    }

    suspend fun startAdHocWorkout(name: String = "Ad-hoc Workout"): String = withContext(Dispatchers.IO) {
        val existingActive = database.workoutSessionDao().getActiveSessionSync()
        if (existingActive != null) {
            database.workoutSessionDao().deleteSetsForSession(existingActive.id)
            database.workoutSessionDao().deleteSessionExercisesForSession(existingActive.id)
            database.workoutSessionDao().deleteSession(existingActive.id)
        }

        val sessionId = UUID.randomUUID().toString()
        val session = WorkoutSession(
            id = sessionId,
            name = name,
            startTime = System.currentTimeMillis(),
            isCompleted = false
        )
        database.workoutSessionDao().insertSession(session)
        sessionId
    }

    suspend fun addExerciseToActiveWorkout(exerciseId: String) = withContext(Dispatchers.IO) {
        val session = database.workoutSessionDao().getActiveSessionSync() ?: return@withContext
        val exercise = database.exerciseDao().getExerciseByIdSync(exerciseId) ?: return@withContext
        val currentExercises = database.workoutSessionDao().getSessionExercisesSync(session.id)

        val sessionExId = UUID.randomUUID().toString()
        val sessionExercise = SessionExercise(
            id = sessionExId,
            sessionId = session.id,
            exerciseId = exerciseId,
            exerciseName = exercise.name,
            primaryMuscleGroup = exercise.primaryMuscleGroup,
            imagePath = exercise.imagePath,
            orderIndex = currentExercises.size
        )
        database.workoutSessionDao().insertSessionExercise(sessionExercise)

        val profile = database.userDao().getUserProfileSync()
        val preferredUnit = profile?.preferredWeightUnit ?: "kg"

        // Default 3 sets
        val sets = (1..3).map { setNum ->
            WorkoutSet(
                id = UUID.randomUUID().toString(),
                sessionExerciseId = sessionExId,
                sessionId = session.id,
                setNumber = setNum,
                weightUnit = preferredUnit,
                isCompleted = false
            )
        }
        database.workoutSessionDao().insertSets(sets)
    }

    suspend fun removeExerciseFromActiveWorkout(sessionExerciseId: String) = withContext(Dispatchers.IO) {
        database.workoutSessionDao().deleteSetsForExercise(sessionExerciseId)
        database.workoutSessionDao().deleteSessionExercise(sessionExerciseId)
    }

    suspend fun addSetToExercise(sessionExerciseId: String) = withContext(Dispatchers.IO) {
        val currentSets = database.workoutSessionDao().getSetsForExerciseSync(sessionExerciseId)
        val lastSet = currentSets.lastOrNull()
        val newSetNumber = (lastSet?.setNumber ?: 0) + 1
        val newSet = WorkoutSet(
            id = UUID.randomUUID().toString(),
            sessionExerciseId = sessionExerciseId,
            sessionId = lastSet?.sessionId ?: "",
            setNumber = newSetNumber,
            weight = lastSet?.weight,
            reps = lastSet?.reps,
            weightUnit = lastSet?.weightUnit ?: "kg",
            isCompleted = false
        )
        database.workoutSessionDao().insertSet(newSet)
    }

    suspend fun updateSet(set: WorkoutSet) = withContext(Dispatchers.IO) {
        database.workoutSessionDao().updateSet(set)
    }

    suspend fun completeSet(setId: String, completed: Boolean) = withContext(Dispatchers.IO) {
        val sets = database.workoutSessionDao().getSetsForSessionSync(
            database.workoutSessionDao().getActiveSessionSync()?.id ?: ""
        )
        val target = sets.firstOrNull { it.id == setId } ?: return@withContext
        val updated = target.copy(
            isCompleted = completed,
            completedAt = if (completed) System.currentTimeMillis() else null
        )
        database.workoutSessionDao().updateSet(updated)
    }

    suspend fun removeSet(setId: String) = withContext(Dispatchers.IO) {
        database.workoutSessionDao().deleteSet(setId)
    }

    suspend fun finishActiveWorkout(): WorkoutSession? = withContext(Dispatchers.IO) {
        val activeSession = database.workoutSessionDao().getActiveSessionSync() ?: return@withContext null
        val exercises = database.workoutSessionDao().getSessionExercisesSync(activeSession.id)
        val allSets = database.workoutSessionDao().getSetsForSessionSync(activeSession.id)
        // Update and save sets where weight & reps were entered
        val validSets = allSets.map { set ->
            if (!set.isCompleted && (set.weight ?: 0.0) > 0.0 && (set.reps ?: 0) > 0) {
                set.copy(isCompleted = true, completedAt = System.currentTimeMillis())
            } else set
        }
        validSets.forEach { database.workoutSessionDao().updateSet(it) }

        val completedSets = validSets.filter { it.isCompleted && (it.weight ?: 0.0) > 0.0 && (it.reps ?: 0) > 0 }
        val totalVolume = completedSets.sumOf { it.volume }
        val totalSetsCount = completedSets.size
        val totalRepsCount = completedSets.sumOf { it.reps ?: 0 }
        val endTime = System.currentTimeMillis()
        val durationSec = ((endTime - activeSession.startTime) / 1000L).coerceAtLeast(0L)

        val finishedSession = activeSession.copy(
            endTime = endTime,
            isCompleted = true,
            totalVolume = totalVolume,
            totalSets = totalSetsCount,
            totalReps = totalRepsCount,
            durationSeconds = durationSec
        )
        database.workoutSessionDao().updateSession(finishedSession)
        finishedSession
    }

    suspend fun discardActiveWorkout() = withContext(Dispatchers.IO) {
        val active = database.workoutSessionDao().getActiveSessionSync() ?: return@withContext
        database.workoutSessionDao().deleteSetsForSession(active.id)
        database.workoutSessionDao().deleteSessionExercisesForSession(active.id)
        database.workoutSessionDao().deleteSession(active.id)
    }

    suspend fun logCompletedExerciseSession(
        exerciseId: String,
        sessionName: String,
        setsInput: List<QuickSetInput>
    ): WorkoutSession = withContext(Dispatchers.IO) {
        val exercise = database.exerciseDao().getExerciseByIdSync(exerciseId)
        val sessionId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // Valid sets must have weight > 0 and reps > 0
        val validSets = setsInput.filter { (it.weight ?: 0.0) > 0.0 && (it.reps ?: 0) > 0 }
        val totalVolume = validSets.sumOf { (it.weight ?: 0.0) * (it.reps ?: 0) }
        val totalReps = validSets.sumOf { it.reps ?: 0 }
        val totalSets = validSets.size

        val durationSec = (validSets.size * 180L).coerceAtLeast(300L)
        val startTime = now - (durationSec * 1000L)

        val session = WorkoutSession(
            id = sessionId,
            name = sessionName,
            startTime = startTime,
            endTime = now,
            durationSeconds = durationSec,
            totalVolume = totalVolume,
            totalSets = totalSets,
            totalReps = totalReps,
            isCompleted = true
        )
        database.workoutSessionDao().insertSession(session)

        val sessionExId = UUID.randomUUID().toString()
        val sessionExercise = SessionExercise(
            id = sessionExId,
            sessionId = sessionId,
            exerciseId = exerciseId,
            exerciseName = exercise?.name ?: "Exercise",
            primaryMuscleGroup = exercise?.primaryMuscleGroup ?: "Other",
            imagePath = exercise?.imagePath,
            orderIndex = 0
        )
        database.workoutSessionDao().insertSessionExercise(sessionExercise)

        val dbSets = validSets.mapIndexed { idx, s ->
            val estimated1RM = if (s.weight != null && s.reps != null && s.reps > 0) {
                s.weight * (1.0 + (s.reps / 30.0))
            } else null
            WorkoutSet(
                id = UUID.randomUUID().toString(),
                sessionExerciseId = sessionExId,
                sessionId = sessionId,
                setNumber = idx + 1,
                weight = s.weight,
                reps = s.reps,
                weightUnit = s.weightUnit,
                isCompleted = true,
                completedAt = now
            )
        }
        database.workoutSessionDao().insertSets(dbSets)

        session
    }

    fun getCompletedExerciseIdsToday(): Flow<List<String>> {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = start + (24L * 3600 * 1000)
        return database.workoutSessionDao().getCompletedExerciseIdsBetween(start, end)
    }

    fun getCompletedSessionsBetween(start: Long, end: Long): Flow<List<WorkoutSession>> {
        return database.workoutSessionDao().getCompletedSessionsBetween(start, end)
    }

    // ----------------------------------------------------
    // Progress & Reports
    // ----------------------------------------------------
    fun getProgressSummary(startTime: Long, endTime: Long): Flow<ProgressSummary> {
        return database.workoutSessionDao().getCompletedSessionsBetween(startTime, endTime).combine(
            database.bodyWeightDao().getAllEntries()
        ) { sessions, weightEntries ->
            val totalWorkouts = sessions.size
            val totalVolume = sessions.sumOf { it.totalVolume }
            val totalSets = sessions.sumOf { it.totalSets }
            val totalReps = sessions.sumOf { it.totalReps }

            // Compute muscle group volume distribution
            val muscleVolume = mutableMapOf<String, Double>()
            for (session in sessions) {
                val exercises = database.workoutSessionDao().getSessionExercisesSync(session.id)
                for (ex in exercises) {
                    val sets = database.workoutSessionDao().getSetsForExerciseSync(ex.id)
                    val vol = sets.filter { it.isCompleted }.sumOf { it.volume }
                    if (vol > 0) {
                        muscleVolume[ex.primaryMuscleGroup] = (muscleVolume[ex.primaryMuscleGroup] ?: 0.0) + vol
                    }
                }
            }

            val currentWeight = weightEntries.firstOrNull()?.weight
            val weightChange = if (weightEntries.size >= 2) {
                val first = weightEntries.first().weight
                val last = weightEntries.last().weight
                first - last
            } else null

            // Consistency rate: percentage of planned workout days completed in this window
            val daysInWindow = ((endTime - startTime) / (24L * 3600 * 1000)).coerceAtLeast(1)
            val expectedWorkouts = (daysInWindow * 4 / 7).coerceAtLeast(1) // based on typical 4-5 day split
            val consistency = ((totalWorkouts.toDouble() / expectedWorkouts.toDouble()) * 100).toInt().coerceIn(0, 100)

            ProgressSummary(
                completedWorkoutsCount = totalWorkouts,
                completedSetsCount = totalSets,
                totalReps = totalReps,
                totalVolume = totalVolume,
                consistencyRatePercent = consistency,
                currentWeight = currentWeight,
                weightChange = weightChange,
                muscleGroupVolumeMap = muscleVolume
            )
        }.flowOn(Dispatchers.IO)
    }
}
