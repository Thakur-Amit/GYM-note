package com.example.data.backup

import com.example.data.local.AppDatabase
import com.example.data.model.BodyWeightEntry
import com.example.data.model.Exercise
import com.example.data.model.ScheduleDay
import com.example.data.model.SessionExercise
import com.example.data.model.TemplateExercise
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutSchedule
import com.example.data.model.WorkoutSession
import com.example.data.model.WorkoutSet
import com.example.data.model.WorkoutTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupManager(private val database: AppDatabase) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "Gym Tracker")
        root.put("exportedAt", System.currentTimeMillis())

        // 1. Profile
        val profile = database.userDao().getUserProfileSync()
        profile?.let {
            val pObj = JSONObject().apply {
                put("name", it.name)
                put("dateOfBirth", it.dateOfBirth ?: 0L)
                put("heightCm", it.heightCm ?: 0.0)
                put("preferredWeightUnit", it.preferredWeightUnit)
                put("fitnessJourneyStartDate", it.fitnessJourneyStartDate ?: 0L)
                put("fitnessGoal", it.fitnessGoal ?: "")
                put("personalNotes", it.personalNotes ?: "")
            }
            root.put("profile", pObj)
        }

        // 2. Body Weight Entries
        val weightArray = JSONArray()
        val allWeights = database.bodyWeightDao().getAllEntriesSync()
        for (w in allWeights) {
            val wObj = JSONObject().apply {
                put("id", w.id)
                put("weight", w.weight)
                put("weightUnit", w.weightUnit)
                if (w.bodyFatPercentage != null) put("bodyFatPercentage", w.bodyFatPercentage)
                if (w.muscleMass != null) put("muscleMass", w.muscleMass)
                put("date", w.date)
                put("notes", w.notes ?: "")
            }
            weightArray.put(wObj)
        }
        root.put("bodyWeight", weightArray)

        // 3. Exercises
        val exercises = database.exerciseDao().getExerciseByIdSync("barbell_bench_press")
        val exArray = JSONArray()
        root.put("exercises", exArray)

        // 4. Workout Sessions
        val sessionArray = JSONArray()
        val sessions = database.workoutSessionDao().getCompletedSessionsSync()
        for (session in sessions) {
            val sObj = JSONObject().apply {
                put("id", session.id)
                put("name", session.name)
                put("templateId", session.templateId ?: "")
                put("startTime", session.startTime)
                put("endTime", session.endTime ?: 0L)
                put("isCompleted", session.isCompleted)
                put("totalVolume", session.totalVolume)
                put("totalSets", session.totalSets)
                put("totalReps", session.totalReps)
                put("durationSeconds", session.durationSeconds)
                put("notes", session.notes ?: "")

                val exList = database.workoutSessionDao().getSessionExercisesSync(session.id)
                val exListArray = JSONArray()
                for (se in exList) {
                    val seObj = JSONObject().apply {
                        put("id", se.id)
                        put("exerciseId", se.exerciseId)
                        put("exerciseName", se.exerciseName)
                        put("primaryMuscleGroup", se.primaryMuscleGroup)
                        put("orderIndex", se.orderIndex)

                        val sets = database.workoutSessionDao().getSetsForExerciseSync(se.id)
                        val setsArray = JSONArray()
                        for (set in sets) {
                            val setObj = JSONObject().apply {
                                put("id", set.id)
                                put("setNumber", set.setNumber)
                                put("setTag", set.setTag)
                                put("weight", set.weight ?: 0.0)
                                put("reps", set.reps ?: 0)
                                put("weightUnit", set.weightUnit)
                                put("isCompleted", set.isCompleted)
                                put("completedAt", set.completedAt ?: 0L)
                            }
                            setsArray.put(setObj)
                        }
                        put("sets", setsArray)
                    }
                    exListArray.put(seObj)
                }
                put("exercises", exListArray)
            }
            sessionArray.put(sObj)
        }
        root.put("sessions", sessionArray)

        root.toString(2)
    }

    suspend fun exportWorkoutsToCsv(): String = withContext(Dispatchers.IO) {
        val sessions = database.workoutSessionDao().getCompletedSessionsSync()
        val sb = StringBuilder()
        sb.append("Session Date,Workout Name,Duration (min),Total Volume,Total Sets,Total Reps,Exercise Name,Set #,Weight,Reps,Completed\n")

        for (session in sessions) {
            val dateStr = dateFormat.format(Date(session.startTime))
            val durationMin = session.durationSeconds / 60
            val exercises = database.workoutSessionDao().getSessionExercisesSync(session.id)
            for (exercise in exercises) {
                val sets = database.workoutSessionDao().getSetsForExerciseSync(exercise.id)
                for (set in sets) {
                    sb.append("\"$dateStr\",")
                    sb.append("\"${session.name}\",")
                    sb.append("$durationMin,")
                    sb.append("${session.totalVolume},")
                    sb.append("${session.totalSets},")
                    sb.append("${session.totalReps},")
                    sb.append("\"${exercise.exerciseName}\",")
                    sb.append("${set.setNumber},")
                    sb.append("${set.weight ?: 0.0},")
                    sb.append("${set.reps ?: 0},")
                    sb.append("${set.isCompleted}\n")
                }
            }
        }
        sb.toString()
    }

    suspend fun exportBodyWeightToCsv(): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.append("Date,Weight,Unit,Body Fat %,Muscle Mass,Notes\n")
        val allEntries = database.bodyWeightDao().getAllEntriesSync()
        for (entry in allEntries) {
            val bf = entry.bodyFatPercentage?.toString() ?: ""
            val mm = entry.muscleMass?.toString() ?: ""
            sb.append("\"${dateFormat.format(Date(entry.date))}\",${entry.weight},${entry.weightUnit},\"$bf\",\"$mm\",\"${entry.notes ?: ""}\"\n")
        }
        sb.toString()
    }

    suspend fun restoreFromJson(jsonString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("app") || root.optString("app") != "Gym Tracker") {
                return@withContext Result.failure(IllegalArgumentException("Invalid Gym Tracker backup format"))
            }

            // Restore Profile
            if (root.has("profile")) {
                val pObj = root.getJSONObject("profile")
                val current = database.userDao().getUserProfileSync() ?: UserProfile()
                val updated = current.copy(
                    name = pObj.optString("name", current.name),
                    dateOfBirth = if (pObj.optLong("dateOfBirth", 0L) > 0) pObj.getLong("dateOfBirth") else current.dateOfBirth,
                    heightCm = pObj.optDouble("heightCm", current.heightCm ?: 175.0),
                    preferredWeightUnit = pObj.optString("preferredWeightUnit", current.preferredWeightUnit),
                    fitnessGoal = pObj.optString("fitnessGoal", current.fitnessGoal ?: ""),
                    personalNotes = pObj.optString("personalNotes", current.personalNotes ?: "")
                )
                database.userDao().insertOrUpdate(updated)
            }

            // Restore Body Weights
            if (root.has("bodyWeight")) {
                val bwArray = root.getJSONArray("bodyWeight")
                for (i in 0 until bwArray.length()) {
                    val wObj = bwArray.getJSONObject(i)
                    val entry = BodyWeightEntry(
                        id = wObj.optLong("id", 0L),
                        weight = wObj.optDouble("weight", 70.0),
                        weightUnit = wObj.optString("weightUnit", "kg"),
                        bodyFatPercentage = if (wObj.has("bodyFatPercentage")) wObj.getDouble("bodyFatPercentage") else null,
                        muscleMass = if (wObj.has("muscleMass")) wObj.getDouble("muscleMass") else null,
                        date = wObj.optLong("date", System.currentTimeMillis()),
                        notes = if (wObj.has("notes") && wObj.getString("notes").isNotEmpty()) wObj.getString("notes") else null
                    )
                    database.bodyWeightDao().insert(entry)
                }
            }

            // Restore Sessions
            if (root.has("sessions")) {
                val sessionsArray = root.getJSONArray("sessions")
                for (i in 0 until sessionsArray.length()) {
                    val sObj = sessionsArray.getJSONObject(i)
                    val sessionId = sObj.getString("id")
                    val session = WorkoutSession(
                        id = sessionId,
                        name = sObj.optString("name", "Restored Workout"),
                        templateId = if (sObj.has("templateId")) sObj.getString("templateId") else null,
                        startTime = sObj.optLong("startTime", System.currentTimeMillis()),
                        endTime = if (sObj.has("endTime") && sObj.getLong("endTime") > 0) sObj.getLong("endTime") else null,
                        isCompleted = sObj.optBoolean("isCompleted", true),
                        notes = sObj.optString("notes", ""),
                        totalVolume = sObj.optDouble("totalVolume", 0.0),
                        totalSets = sObj.optInt("totalSets", 0),
                        totalReps = sObj.optInt("totalReps", 0),
                        durationSeconds = sObj.optLong("durationSeconds", 0L)
                    )
                    database.workoutSessionDao().insertSession(session)

                    if (sObj.has("exercises")) {
                        val exArray = sObj.getJSONArray("exercises")
                        for (j in 0 until exArray.length()) {
                            val seObj = exArray.getJSONObject(j)
                            val seId = seObj.getString("id")
                            val se = SessionExercise(
                                id = seId,
                                sessionId = sessionId,
                                exerciseId = seObj.getString("exerciseId"),
                                exerciseName = seObj.optString("exerciseName", "Exercise"),
                                primaryMuscleGroup = seObj.optString("primaryMuscleGroup", "Other"),
                                orderIndex = seObj.optInt("orderIndex", j)
                            )
                            database.workoutSessionDao().insertSessionExercise(se)

                            if (seObj.has("sets")) {
                                val setArray = seObj.getJSONArray("sets")
                                for (k in 0 until setArray.length()) {
                                    val setObj = setArray.getJSONObject(k)
                                    val set = WorkoutSet(
                                        id = setObj.getString("id"),
                                        sessionExerciseId = seId,
                                        sessionId = sessionId,
                                        setNumber = setObj.optInt("setNumber", k + 1),
                                        setTag = setObj.optString("setTag", "NORMAL"),
                                        weight = setObj.optDouble("weight", 0.0),
                                        reps = setObj.optInt("reps", 0),
                                        weightUnit = setObj.optString("weightUnit", "kg"),
                                        isCompleted = setObj.optBoolean("isCompleted", true),
                                        completedAt = setObj.optLong("completedAt", 0L)
                                    )
                                    database.workoutSessionDao().insertSet(set)
                                }
                            }
                        }
                    }
                }
            }

            Result.success("Backup restored successfully!")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
