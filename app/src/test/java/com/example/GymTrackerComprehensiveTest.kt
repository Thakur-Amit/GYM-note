package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import com.example.data.model.BodyWeightEntry
import com.example.data.model.Exercise
import com.example.data.model.MuscleGroup
import com.example.data.model.UserProfile
import com.example.data.model.WeightUnit
import com.example.data.model.WorkoutSession
import com.example.data.model.WorkoutSet
import com.example.data.model.WorkoutTemplate
import com.example.data.repository.GymRepository
import com.example.data.storage.ImageStorageManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GymTrackerComprehensiveTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: GymRepository
    private lateinit var backupManager: BackupManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val imageStorageManager = ImageStorageManager(context)
        repository = GymRepository(database, imageStorageManager)
        backupManager = BackupManager(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAgeCalculation() {
        val cal = Calendar.getInstance().apply {
            add(Calendar.YEAR, -25)
        }
        val profile = UserProfile(dateOfBirth = cal.timeInMillis)
        val age = profile.calculateAge()
        assertEquals(25, age)
    }

    @Test
    fun testWeightUnitConversion() {
        val kg = 100.0
        val lb = WeightUnit.KG.convertTo(WeightUnit.LB, kg)
        assertEquals(220.46, lb, 0.1)

        val convertedBack = WeightUnit.LB.convertTo(WeightUnit.KG, lb)
        assertEquals(100.0, convertedBack, 0.1)
    }

    @Test
    fun testVolumeCalculationAndEstimated1RM() {
        val set = WorkoutSet(
            id = "test_set_1",
            sessionExerciseId = "se_1",
            sessionId = "s_1",
            setNumber = 1,
            weight = 100.0,
            reps = 10,
            isCompleted = true
        )
        // Volume = 100 * 10 = 1000
        assertEquals(1000.0, set.volume, 0.01)

        // Estimated 1RM (Epley formula: 100 * (1 + 10 / 30)) = 133.33
        val est1RM = set.estimated1RM
        assertNotNull(est1RM)
        assertEquals(133.33, est1RM!!, 0.1)
    }

    @Test
    fun testAddAndRetrieveBodyWeight() = runBlocking {
        repository.addBodyWeightEntry(76.5, "kg", notes = "Morning weight")
        val latest = repository.latestBodyWeight.first()
        assertNotNull(latest)
        assertEquals(76.5, latest!!.weight, 0.01)
        assertEquals("Morning weight", latest.notes)
    }

    @Test
    fun testExerciseCreationAndQuery() = runBlocking {
        val exercise = Exercise(
            id = "test_bench_press",
            name = "Test Bench Press",
            primaryMuscleGroup = MuscleGroup.CHEST.name,
            equipment = "Barbell"
        )
        repository.saveExercise(exercise)
        val retrieved = repository.getExerciseById("test_bench_press").first()
        assertNotNull(retrieved)
        assertEquals("Test Bench Press", retrieved!!.name)
        assertEquals(MuscleGroup.CHEST.name, retrieved.primaryMuscleGroup)
    }

    @Test
    fun testStartWorkoutAndAutosaveRecovery() = runBlocking {
        // Start ad-hoc workout
        val sessionId = repository.startAdHocWorkout("Test Workout Session")
        val active = repository.activeSession.first()
        assertNotNull(active)
        assertEquals(sessionId, active!!.session.id)
        assertEquals("Test Workout Session", active.session.name)

        // Add exercise
        val exercise = Exercise(
            id = "squat_test",
            name = "Squat Test",
            primaryMuscleGroup = MuscleGroup.LEGS.name
        )
        repository.saveExercise(exercise)
        repository.addExerciseToActiveWorkout("squat_test")

        val withEx = repository.activeSession.first()
        assertNotNull(withEx)
        assertEquals(1, withEx!!.exercises.size)
        assertEquals("Squat Test", withEx.exercises[0].exercise.exerciseName)

        // Complete a set
        val firstSet = withEx.exercises[0].sets[0]
        repository.updateSet(firstSet.copy(weight = 80.0, reps = 8))
        repository.completeSet(firstSet.id, true)

        val updated = repository.activeSession.first()
        val completedSet = updated!!.exercises[0].sets.first { it.id == firstSet.id }
        assertTrue(completedSet.isCompleted)
        assertEquals(80.0, completedSet.weight ?: 0.0, 0.01)
        assertEquals(8, completedSet.reps)

        // Finish workout
        val finished = repository.finishActiveWorkout()
        assertNotNull(finished)
        assertTrue(finished!!.isCompleted)
        assertEquals(640.0, finished.totalVolume, 0.01) // 80 * 8 = 640

        // Active session should now be null
        val afterFinish = repository.activeSession.first()
        assertNull(afterFinish)
    }

    @Test
    fun testBackupAndRestore() = runBlocking {
        // Add a profile and completed workout
        repository.updateProfile(UserProfile(name = "Champion Lifter", preferredWeightUnit = "kg"))
        val sessionId = repository.startAdHocWorkout("Backup Test Workout")
        repository.finishActiveWorkout()

        val json = backupManager.exportToJson()
        assertTrue(json.contains("Gym Tracker"))
        assertTrue(json.contains("Champion Lifter"))

        // Restore into database
        val result = backupManager.restoreFromJson(json)
        assertTrue(result.isSuccess)

        val restoredProfile = repository.userProfile.first()
        assertEquals("Champion Lifter", restoredProfile!!.name)
    }
}
