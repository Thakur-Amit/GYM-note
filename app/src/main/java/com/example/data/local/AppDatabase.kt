package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BodyWeightEntry
import com.example.data.model.Exercise
import com.example.data.model.ExerciseType
import com.example.data.model.LoadEntryMode
import com.example.data.model.MuscleGroup
import com.example.data.model.ScheduleDay
import com.example.data.model.SessionExercise
import com.example.data.model.TemplateExercise
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutSchedule
import com.example.data.model.WorkoutSession
import com.example.data.model.WorkoutSet
import com.example.data.model.WorkoutTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        UserProfile::class,
        BodyWeightEntry::class,
        Exercise::class,
        WorkoutTemplate::class,
        TemplateExercise::class,
        WorkoutSchedule::class,
        ScheduleDay::class,
        WorkoutSession::class,
        SessionExercise::class,
        WorkoutSet::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bodyWeightDao(): BodyWeightDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun templateDao(): TemplateDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun workoutSessionDao(): WorkoutSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_tracker.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    seedInitialData(database)
                }
            }
        }
    }
}

suspend fun seedInitialData(database: AppDatabase) {
    // 1. Initial User Profile (Clean initial state without dummy values)
    val defaultProfile = UserProfile(
        id = 1,
        name = "Athlete",
        profilePicturePath = null,
        dateOfBirth = null,
        heightCm = 175.0,
        preferredWeightUnit = "kg",
        fitnessJourneyStartDate = System.currentTimeMillis(),
        fitnessGoal = "Build Strength & Lean Muscle",
        personalNotes = null
    )
    database.userDao().insertOrUpdate(defaultProfile)

    // No dummy body weight entries seeded - user logs their own genuine measurements

    // 2. Built-in Exercise Catalog
    val defaultExercises = listOf(
        Exercise(
            id = "barbell_bench_press",
            name = "Barbell Bench Press",
            primaryMuscleGroup = MuscleGroup.CHEST.name,
            secondaryMuscleGroups = "Triceps, Shoulders",
            exerciseType = ExerciseType.BARBELL.name,
            equipment = "Barbell, Flat Bench",
            instructions = "Lie on a flat bench. Grip the barbell slightly wider than shoulder width. Lower bar to mid-chest with control, flare elbows around 45-75 degrees, and press explosively up.",
            imagePath = "res:ex_bench_press",
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "incline_dumbbell_press",
            name = "Incline Dumbbell Press",
            primaryMuscleGroup = MuscleGroup.CHEST.name,
            secondaryMuscleGroups = "Shoulders, Triceps",
            exerciseType = ExerciseType.DUMBBELL.name,
            equipment = "Dumbbells, Incline Bench (30-45°)",
            instructions = "Set bench to 30-45 degree incline. Press dumbbells upward together while maintaining slight arch in back and retracted scapula.",
            imagePath = null,
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "dumbbell_fly",
            name = "Dumbbell Fly",
            primaryMuscleGroup = MuscleGroup.CHEST.name,
            secondaryMuscleGroups = "Shoulders",
            exerciseType = ExerciseType.DUMBBELL.name,
            equipment = "Dumbbells, Flat Bench",
            instructions = "Lie on a flat bench with slight elbow bend. Lower dumbbells in an arc out to sides until chest stretch is felt, then squeeze back up.",
            imagePath = null,
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "barbell_squat",
            name = "Barbell Squat",
            primaryMuscleGroup = MuscleGroup.LEGS.name,
            secondaryMuscleGroups = "Glutes, Core/Abs, Calves",
            exerciseType = ExerciseType.BARBELL.name,
            equipment = "Barbell, Squat Rack",
            instructions = "Rest bar on upper traps. Feet shoulder-width apart, toes slightly flared. Break at hips and knees simultaneously, descend until thighs parallel or below, press through mid-foot.",
            imagePath = "res:ex_squat",
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "leg_press",
            name = "Leg Press",
            primaryMuscleGroup = MuscleGroup.LEGS.name,
            secondaryMuscleGroups = "Glutes, Calves",
            exerciseType = ExerciseType.MACHINE.name,
            equipment = "45° Incline Leg Press Machine",
            instructions = "Position feet shoulder-width on platform. Release safety handles, lower sledge until 90 degree knee flexion, press back without locking knees hard.",
            imagePath = null,
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "romanian_deadlift",
            name = "Romanian Deadlift",
            primaryMuscleGroup = MuscleGroup.LEGS.name,
            secondaryMuscleGroups = "Glutes, Back, Forearms",
            exerciseType = ExerciseType.BARBELL.name,
            equipment = "Barbell",
            instructions = "Stand holding barbell with overhand grip. Hinge backward at hips with slight soft knees, keep spine neutral and bar close to legs. Squeeze glutes to stand.",
            imagePath = "res:ex_deadlift",
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "lat_pulldown",
            name = "Lat Pulldown",
            primaryMuscleGroup = MuscleGroup.BACK.name,
            secondaryMuscleGroups = "Biceps, Forearms",
            exerciseType = ExerciseType.CABLE.name,
            equipment = "Lat Pulldown Cable Machine",
            instructions = "Grip wide bar with overhand grip. Lean back slightly, drive elbows straight down towards ribs, squeezing lats at the bottom.",
            imagePath = null,
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "barbell_row",
            name = "Barbell Row",
            primaryMuscleGroup = MuscleGroup.BACK.name,
            secondaryMuscleGroups = "Biceps, Shoulders",
            exerciseType = ExerciseType.BARBELL.name,
            equipment = "Barbell",
            instructions = "Bend over at 45 degrees with neutral spine. Pull bar to upper abdomen/navel, keeping elbows tucked close to torso.",
            imagePath = null,
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "seated_cable_row",
            name = "Seated Cable Row",
            primaryMuscleGroup = MuscleGroup.BACK.name,
            secondaryMuscleGroups = "Biceps",
            exerciseType = ExerciseType.CABLE.name,
            equipment = "Seated Cable Row Station",
            instructions = "Sit upright with knees slightly bent. Pull close-grip attachment to torso while retracting shoulder blades fully.",
            imagePath = null,
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "overhead_shoulder_press",
            name = "Overhead Shoulder Press",
            primaryMuscleGroup = MuscleGroup.SHOULDERS.name,
            secondaryMuscleGroups = "Triceps, Core/Abs",
            exerciseType = ExerciseType.BARBELL.name,
            equipment = "Barbell / Dumbbells",
            instructions = "Press weight vertically from collarbones overhead until arms locked out. Keep core tight and avoid leaning backward.",
            imagePath = "res:ex_shoulder_press",
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "lateral_raise",
            name = "Lateral Raise",
            primaryMuscleGroup = MuscleGroup.SHOULDERS.name,
            secondaryMuscleGroups = "Traps",
            exerciseType = ExerciseType.DUMBBELL.name,
            equipment = "Dumbbells",
            instructions = "Hold dumbbells at sides with slight elbow bend. Raise arms laterally up to shoulder height, lead with elbows, control descent.",
            imagePath = null,
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "biceps_curl",
            name = "Biceps Curl",
            primaryMuscleGroup = MuscleGroup.BICEPS.name,
            secondaryMuscleGroups = "Forearms",
            exerciseType = ExerciseType.DUMBBELL.name,
            equipment = "Dumbbells / Barbell",
            instructions = "Hold weights with palms supinated. Curl up towards shoulders without swinging elbows, squeeze peak contraction.",
            imagePath = null,
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "hammer_curl",
            name = "Hammer Curl",
            primaryMuscleGroup = MuscleGroup.BICEPS.name,
            secondaryMuscleGroups = "Forearms",
            exerciseType = ExerciseType.DUMBBELL.name,
            equipment = "Dumbbells",
            instructions = "Hold dumbbells in neutral grip (palms facing each other). Curl up concentrating on brachialis and forearm muscles.",
            imagePath = null,
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "triceps_pushdown",
            name = "Triceps Pushdown",
            primaryMuscleGroup = MuscleGroup.TRICEPS.name,
            secondaryMuscleGroups = "Forearms",
            exerciseType = ExerciseType.CABLE.name,
            equipment = "Cable Machine, Rope or Straight Bar",
            instructions = "Pin upper arms beside ribcage. Extend elbows fully downward, locking out triceps and pausing briefly.",
            imagePath = null,
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "standing_calf_raise",
            name = "Standing Calf Raise",
            primaryMuscleGroup = MuscleGroup.CALVES.name,
            secondaryMuscleGroups = "",
            exerciseType = ExerciseType.MACHINE.name,
            equipment = "Calf Raise Machine / Step",
            instructions = "Balls of feet on edge of platform. Lower heels for deep stretch, press all the way up onto balls of feet, hold peak contraction.",
            imagePath = null,
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        ),
        Exercise(
            id = "plank",
            name = "Plank",
            primaryMuscleGroup = MuscleGroup.CORE_ABS.name,
            secondaryMuscleGroups = "Shoulders, Glutes",
            exerciseType = ExerciseType.BODYWEIGHT.name,
            equipment = "Floor Mat",
            instructions = "Hold push-up position on forearms and toes. Keep straight line from shoulders to heels, engage core tightly.",
            imagePath = null,
            loadEntryMode = LoadEntryMode.DURATION.name
        ),
        Exercise(
            id = "hip_thrust",
            name = "Hip Thrust",
            primaryMuscleGroup = MuscleGroup.GLUTES.name,
            secondaryMuscleGroups = "Legs",
            exerciseType = ExerciseType.BARBELL.name,
            equipment = "Barbell, Bench",
            instructions = "Upper back against bench, barbell across hips. Drive hips straight up through heels until thighs and torso align horizontally.",
            imagePath = null,
            loadEntryMode = LoadEntryMode.WEIGHT_AND_REPS.name
        )
    )
    database.exerciseDao().insertAll(defaultExercises)

    // 3. Reusable Workout Templates
    val templates = listOf(
        WorkoutTemplate(
            id = "template_push",
            name = "Push Day",
            description = "Chest, Shoulders & Triceps hypertrophy focus",
            isCustom = false,
            orderIndex = 0
        ),
        WorkoutTemplate(
            id = "template_pull",
            name = "Pull Day",
            description = "Back, Biceps & Posterior chain focus",
            isCustom = false,
            orderIndex = 1
        ),
        WorkoutTemplate(
            id = "template_legs",
            name = "Leg Day",
            description = "Quads, Hamstrings, Glutes & Calves intensity",
            isCustom = false,
            orderIndex = 2
        ),
        WorkoutTemplate(
            id = "template_upper",
            name = "Upper Body",
            description = "Comprehensive upper body compound circuit",
            isCustom = false,
            orderIndex = 3
        ),
        WorkoutTemplate(
            id = "template_lower",
            name = "Lower Body",
            description = "Lower body power and mobility",
            isCustom = false,
            orderIndex = 4
        ),
        WorkoutTemplate(
            id = "template_full_body",
            name = "Full Body",
            description = "Total-body compound strength session",
            isCustom = false,
            orderIndex = 5
        )
    )
    database.templateDao().insertTemplates(templates)

    // Template exercises
    val pushExercises = listOf(
        TemplateExercise(templateId = "template_push", exerciseId = "barbell_bench_press", orderIndex = 0, targetSets = 4, targetRepRange = "6-10", targetWeight = 70.0),
        TemplateExercise(templateId = "template_push", exerciseId = "incline_dumbbell_press", orderIndex = 1, targetSets = 3, targetRepRange = "8-12", targetWeight = 26.0),
        TemplateExercise(templateId = "template_push", exerciseId = "overhead_shoulder_press", orderIndex = 2, targetSets = 3, targetRepRange = "8-10", targetWeight = 45.0),
        TemplateExercise(templateId = "template_push", exerciseId = "lateral_raise", orderIndex = 3, targetSets = 4, targetRepRange = "12-15", targetWeight = 10.0),
        TemplateExercise(templateId = "template_push", exerciseId = "triceps_pushdown", orderIndex = 4, targetSets = 3, targetRepRange = "10-12", targetWeight = 25.0)
    )
    val pullExercises = listOf(
        TemplateExercise(templateId = "template_pull", exerciseId = "romanian_deadlift", orderIndex = 0, targetSets = 4, targetRepRange = "8-10", targetWeight = 90.0),
        TemplateExercise(templateId = "template_pull", exerciseId = "lat_pulldown", orderIndex = 1, targetSets = 4, targetRepRange = "8-12", targetWeight = 55.0),
        TemplateExercise(templateId = "template_pull", exerciseId = "barbell_row", orderIndex = 2, targetSets = 3, targetRepRange = "8-10", targetWeight = 60.0),
        TemplateExercise(templateId = "template_pull", exerciseId = "seated_cable_row", orderIndex = 3, targetSets = 3, targetRepRange = "10-12", targetWeight = 50.0),
        TemplateExercise(templateId = "template_pull", exerciseId = "biceps_curl", orderIndex = 4, targetSets = 3, targetRepRange = "10-12", targetWeight = 14.0),
        TemplateExercise(templateId = "template_pull", exerciseId = "hammer_curl", orderIndex = 5, targetSets = 3, targetRepRange = "12-15", targetWeight = 12.0)
    )
    val legExercises = listOf(
        TemplateExercise(templateId = "template_legs", exerciseId = "barbell_squat", orderIndex = 0, targetSets = 4, targetRepRange = "6-8", targetWeight = 90.0),
        TemplateExercise(templateId = "template_legs", exerciseId = "leg_press", orderIndex = 1, targetSets = 3, targetRepRange = "10-12", targetWeight = 140.0),
        TemplateExercise(templateId = "template_legs", exerciseId = "romanian_deadlift", orderIndex = 2, targetSets = 3, targetRepRange = "8-10", targetWeight = 80.0),
        TemplateExercise(templateId = "template_legs", exerciseId = "hip_thrust", orderIndex = 3, targetSets = 3, targetRepRange = "10-12", targetWeight = 100.0),
        TemplateExercise(templateId = "template_legs", exerciseId = "standing_calf_raise", orderIndex = 4, targetSets = 4, targetRepRange = "12-15", targetWeight = 40.0)
    )
    database.templateDao().insertTemplateExercises(pushExercises + pullExercises + legExercises)

    // 4. Default 7-day Weekly Schedule
    val schedule = WorkoutSchedule(id = "active_schedule", cycleType = "WEEKLY", cycleLength = 7)
    database.scheduleDao().insertOrUpdateSchedule(schedule)

    val scheduleDays = listOf(
        ScheduleDay(dayIndex = 0, dayName = "Monday", isRestDay = false, templateId = "template_push"),
        ScheduleDay(dayIndex = 1, dayName = "Tuesday", isRestDay = false, templateId = "template_pull"),
        ScheduleDay(dayIndex = 2, dayName = "Wednesday", isRestDay = false, templateId = "template_legs"),
        ScheduleDay(dayIndex = 3, dayName = "Thursday", isRestDay = false, templateId = "template_push"),
        ScheduleDay(dayIndex = 4, dayName = "Friday", isRestDay = false, templateId = "template_pull"),
        ScheduleDay(dayIndex = 5, dayName = "Saturday", isRestDay = false, templateId = "template_legs"),
        ScheduleDay(dayIndex = 6, dayName = "Sunday", isRestDay = true, templateId = null)
    )
    database.scheduleDao().insertScheduleDays(scheduleDays)
}
