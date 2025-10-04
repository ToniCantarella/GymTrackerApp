package com.tonicantarella.gymtracker.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tonicantarella.gymtracker.database.dao.cardio.CardioMetricsDao
import com.tonicantarella.gymtracker.database.dao.cardio.CardioSessionDao
import com.tonicantarella.gymtracker.database.dao.cardio.CardioWorkoutDao
import com.tonicantarella.gymtracker.database.dao.gym.ExerciseDao
import com.tonicantarella.gymtracker.database.dao.gym.GymSessionDao
import com.tonicantarella.gymtracker.database.dao.gym.GymWorkoutDao
import com.tonicantarella.gymtracker.database.dao.gym.SetDao
import com.tonicantarella.gymtracker.database.dao.gym.SetSessionDao
import com.tonicantarella.gymtracker.database.entity.cardio.CardioMetricsEntity
import com.tonicantarella.gymtracker.database.entity.cardio.CardioSessionEntity
import com.tonicantarella.gymtracker.database.entity.cardio.CardioWorkoutEntity
import com.tonicantarella.gymtracker.database.entity.gym.ExerciseEntity
import com.tonicantarella.gymtracker.database.entity.gym.GymSessionEntity
import com.tonicantarella.gymtracker.database.entity.gym.GymWorkoutEntity
import com.tonicantarella.gymtracker.database.entity.gym.SetEntity
import com.tonicantarella.gymtracker.database.entity.gym.SetSessionEntity
import com.tonicantarella.gymtracker.database.entity.gym.WorkoutExerciseCrossRef
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Database(
    entities = [
        GymWorkoutEntity::class,
        CardioWorkoutEntity::class,
        ExerciseEntity::class,
        SetEntity::class,
        SetSessionEntity::class,
        GymSessionEntity::class,
        CardioMetricsEntity::class,
        CardioSessionEntity::class,
        WorkoutExerciseCrossRef::class
    ],
    version = 4,
    autoMigrations = [
        AutoMigration(
            from = 1,
            to = 2,
            spec = DurationToMillisMigration::class
        ),
        AutoMigration(
            from = 2,
            to = 3,
            spec = MillisToDurationMigration::class
        )
    ],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GymDatabase : RoomDatabase() {
    abstract fun gymWorkoutDao(): GymWorkoutDao
    abstract fun cardioWorkoutDao(): CardioWorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun setDao(): SetDao
    abstract fun setSessionDao(): SetSessionDao
    abstract fun gymSessionDao(): GymSessionDao
    abstract fun cardioDao(): CardioMetricsDao
    abstract fun cardioSessionDao(): CardioSessionDao
}

class Converters {
    @TypeConverter
    fun fromUUID(uuid: UUID): String = uuid.toString()

    @TypeConverter
    fun toUUID(uuid: String): UUID = UUID.fromString(uuid)

    @TypeConverter
    fun fromInstant(instant: Instant): Long = instant.toEpochMilli()

    @TypeConverter
    fun toInstant(millis: Long): Instant = Instant.ofEpochMilli(millis)

    @TypeConverter
    fun fromDuration(duration: Duration): Long  = duration.toMillis()

    @TypeConverter
    fun toDuration(millis: Long): Duration = Duration.ofMillis(millis)
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS workout_exercise_cross_ref (
                workoutId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                PRIMARY KEY(workoutId, exerciseId),
                FOREIGN KEY(workoutId) REFERENCES GymWorkouts(id) ON DELETE CASCADE,
                FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON DELETE CASCADE
            )
        """)

        db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercise_cross_ref_workoutId ON workout_exercise_cross_ref(workoutId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercise_cross_ref_exerciseId ON workout_exercise_cross_ref(exerciseId)")
    }
}

@RenameColumn(
    tableName = "cardio_metrics",
    fromColumnName = "duration",
    toColumnName = "durationMillis"
)
@RenameColumn(
    tableName = "cardio_sessions",
    fromColumnName = "duration",
    toColumnName = "durationMillis"
)
class DurationToMillisMigration : AutoMigrationSpec

@RenameColumn(
    tableName = "cardio_metrics",
    fromColumnName = "durationMillis",
    toColumnName = "duration"
)
@RenameColumn(
    tableName = "cardio_sessions",
    fromColumnName = "durationMillis",
    toColumnName = "duration"
)
class MillisToDurationMigration : AutoMigrationSpec