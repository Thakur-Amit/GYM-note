package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfile)

    @Query("UPDATE user_profile SET profilePicturePath = :path WHERE id = 1")
    suspend fun updateProfilePicture(path: String?)
}

@Dao
interface BodyWeightDao {
    @Query("SELECT * FROM body_weight_entries ORDER BY date DESC, createdAt DESC")
    fun getAllEntries(): Flow<List<BodyWeightEntry>>

    @Query("SELECT * FROM body_weight_entries ORDER BY date DESC, createdAt DESC LIMIT 1")
    fun getLatestEntry(): Flow<BodyWeightEntry?>

    @Query("SELECT * FROM body_weight_entries ORDER BY date DESC, createdAt DESC LIMIT 1")
    suspend fun getLatestEntrySync(): BodyWeightEntry?

    @Query("SELECT * FROM body_weight_entries ORDER BY date DESC, createdAt DESC")
    suspend fun getAllEntriesSync(): List<BodyWeightEntry>

    @Query("SELECT * FROM body_weight_entries ORDER BY date DESC, createdAt DESC LIMIT 1 OFFSET 1")
    suspend fun getPreviousEntrySync(): BodyWeightEntry?

    @Query("SELECT * FROM body_weight_entries WHERE date >= :start AND date <= :end ORDER BY date ASC")
    fun getEntriesBetween(start: Long, end: Long): Flow<List<BodyWeightEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BodyWeightEntry): Long

    @Update
    suspend fun update(entry: BodyWeightEntry)

    @Delete
    suspend fun delete(entry: BodyWeightEntry)

    @Query("DELETE FROM body_weight_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM body_weight_entries")
    suspend fun deleteAll()
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllActiveExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    fun getExerciseById(id: String): Flow<Exercise?>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getExerciseByIdSync(id: String): Exercise?

    @Query("SELECT * FROM exercises WHERE primaryMuscleGroup = :group AND isArchived = 0 ORDER BY name ASC")
    fun getExercisesByMuscleGroup(group: String): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE (name LIKE '%' || :query || '%' OR primaryMuscleGroup LIKE '%' || :query || '%') AND isArchived = 0 ORDER BY name ASC")
    fun searchExercises(query: String): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: Exercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<Exercise>)

    @Update
    suspend fun update(exercise: Exercise)

    @Query("UPDATE exercises SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)

    @Delete
    suspend fun delete(exercise: Exercise)

    @Query("DELETE FROM exercises")
    suspend fun deleteAll()
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM workout_templates WHERE isArchived = 0 ORDER BY orderIndex ASC, createdAt ASC")
    fun getAllTemplates(): Flow<List<WorkoutTemplate>>

    @Query("SELECT * FROM workout_templates WHERE id = :id LIMIT 1")
    fun getTemplateById(id: String): Flow<WorkoutTemplate?>

    @Query("SELECT * FROM workout_templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateByIdSync(id: String): WorkoutTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<WorkoutTemplate>)

    @Update
    suspend fun updateTemplate(template: WorkoutTemplate)

    @Delete
    suspend fun deleteTemplate(template: WorkoutTemplate)

    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY orderIndex ASC")
    fun getExercisesForTemplate(templateId: String): Flow<List<TemplateExercise>>

    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY orderIndex ASC")
    suspend fun getExercisesForTemplateSync(templateId: String): List<TemplateExercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercise(templateExercise: TemplateExercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercises(list: List<TemplateExercise>)

    @Query("DELETE FROM template_exercises WHERE templateId = :templateId")
    suspend fun deleteExercisesForTemplate(templateId: String)

    @Query("DELETE FROM workout_templates")
    suspend fun deleteAllTemplates()

    @Query("DELETE FROM template_exercises")
    suspend fun deleteAllTemplateExercises()
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM workout_schedules WHERE id = 'active_schedule' LIMIT 1")
    fun getSchedule(): Flow<WorkoutSchedule?>

    @Query("SELECT * FROM workout_schedules WHERE id = 'active_schedule' LIMIT 1")
    suspend fun getScheduleSync(): WorkoutSchedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSchedule(schedule: WorkoutSchedule)

    @Query("SELECT * FROM schedule_days WHERE scheduleId = 'active_schedule' ORDER BY dayIndex ASC")
    fun getScheduleDays(): Flow<List<ScheduleDay>>

    @Query("SELECT * FROM schedule_days WHERE scheduleId = 'active_schedule' ORDER BY dayIndex ASC")
    suspend fun getScheduleDaysSync(): List<ScheduleDay>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleDays(days: List<ScheduleDay>)

    @Update
    suspend fun updateScheduleDay(day: ScheduleDay)

    @Query("DELETE FROM schedule_days WHERE scheduleId = 'active_schedule'")
    suspend fun deleteScheduleDays()
}

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    fun getActiveSession(): Flow<WorkoutSession?>

    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSessionSync(): WorkoutSession?

    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    fun getSessionById(id: String): Flow<WorkoutSession?>

    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionByIdSync(id: String): WorkoutSession?

    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 1 ORDER BY startTime DESC")
    fun getCompletedSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 1 ORDER BY startTime DESC")
    suspend fun getCompletedSessionsSync(): List<WorkoutSession>

    @Query("SELECT * FROM workout_sessions WHERE isCompleted = 1 AND startTime >= :start AND startTime <= :end ORDER BY startTime DESC")
    fun getCompletedSessionsBetween(start: Long, end: Long): Flow<List<WorkoutSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession)

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAllSessions()

    // Session Exercises
    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun getSessionExercises(sessionId: String): Flow<List<SessionExercise>>

    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    suspend fun getSessionExercisesSync(sessionId: String): List<SessionExercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionExercise(exercise: SessionExercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionExercises(exercises: List<SessionExercise>)

    @Update
    suspend fun updateSessionExercise(exercise: SessionExercise)

    @Query("DELETE FROM session_exercises WHERE id = :id")
    suspend fun deleteSessionExercise(id: String)

    @Query("DELETE FROM session_exercises WHERE sessionId = :sessionId")
    suspend fun deleteSessionExercisesForSession(sessionId: String)

    // Workout Sets
    @Query("SELECT * FROM workout_sets WHERE sessionExerciseId = :sessionExerciseId ORDER BY setNumber ASC")
    fun getSetsForExercise(sessionExerciseId: String): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM workout_sets WHERE sessionExerciseId = :sessionExerciseId ORDER BY setNumber ASC")
    suspend fun getSetsForExerciseSync(sessionExerciseId: String): List<WorkoutSet>

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    fun getSetsForSession(sessionId: String): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    suspend fun getSetsForSessionSync(sessionId: String): List<WorkoutSet>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<WorkoutSet>)

    @Update
    suspend fun updateSet(set: WorkoutSet)

    @Query("DELETE FROM workout_sets WHERE id = :setId")
    suspend fun deleteSet(setId: String)

    @Query("DELETE FROM workout_sets WHERE sessionExerciseId = :sessionExerciseId")
    suspend fun deleteSetsForExercise(sessionExerciseId: String)

    @Query("DELETE FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: String)

    // History query for specific exercise
    @Query("""
        SELECT s.* FROM workout_sets s 
        INNER JOIN session_exercises se ON s.sessionExerciseId = se.id 
        INNER JOIN workout_sessions ws ON se.sessionId = ws.id 
        WHERE se.exerciseId = :exerciseId AND ws.isCompleted = 1 AND s.isCompleted = 1 
        ORDER BY ws.startTime DESC, s.setNumber ASC
    """)
    fun getCompletedSetsForExerciseHistory(exerciseId: String): Flow<List<WorkoutSet>>

    @Query("""
        SELECT ws.* FROM workout_sessions ws
        INNER JOIN session_exercises se ON ws.id = se.sessionId
        WHERE se.exerciseId = :exerciseId AND ws.isCompleted = 1
        ORDER BY ws.startTime DESC
    """)
    fun getCompletedSessionsForExercise(exerciseId: String): Flow<List<WorkoutSession>>
}
