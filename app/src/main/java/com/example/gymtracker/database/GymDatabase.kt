package com.example.gymtracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gymtracker.database.dao.cardio.CardioDao
import com.example.gymtracker.database.dao.cardio.CardioSessionDao
import com.example.gymtracker.database.dao.cardio.CardioWorkoutPlanDao
import com.example.gymtracker.database.dao.gym.ExerciseDao
import com.example.gymtracker.database.dao.gym.GymSessionDao
import com.example.gymtracker.database.dao.gym.GymWorkoutPlanDao
import com.example.gymtracker.database.dao.gym.SetDao
import com.example.gymtracker.database.dao.gym.SetSessionDao
import com.example.gymtracker.database.entity.cardio.CardioEntity
import com.example.gymtracker.database.entity.cardio.CardioSessionEntity
import com.example.gymtracker.database.entity.cardio.CardioWorkoutPlanEntity
import com.example.gymtracker.database.entity.gym.ExerciseEntity
import com.example.gymtracker.database.entity.gym.GymSessionEntity
import com.example.gymtracker.database.entity.gym.GymWorkoutPlanEntity
import com.example.gymtracker.database.entity.gym.SetEntity
import com.example.gymtracker.database.entity.gym.SetSessionEntity
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Database(
    entities = [
        GymWorkoutPlanEntity::class,
        CardioWorkoutPlanEntity::class,
        ExerciseEntity::class,
        SetEntity::class,
        SetSessionEntity::class,
        GymSessionEntity::class,
        CardioEntity::class,
        CardioSessionEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class GymDatabase : RoomDatabase() {
    abstract fun gymWorkoutPlanDao(): GymWorkoutPlanDao
    abstract fun cardioWorkoutPlanDao(): CardioWorkoutPlanDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun setDao(): SetDao
    abstract fun setSessionDao(): SetSessionDao
    abstract fun gymSessionDao(): GymSessionDao
    abstract fun cardioDao(): CardioDao
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
    fun fromDuration(duration: Duration): Long = duration.toMillis()

    @TypeConverter
    fun toDuration(millis: Long): Duration = Duration.ofMillis(millis)
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS GymWorkoutPlans (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS CardioWorkoutPlans (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO GymWorkoutPlans (id, name)
            SELECT id, name FROM workouts WHERE type = 'GYM' OR type = 0
        """.trimIndent())

        db.execSQL("""
            INSERT INTO CardioWorkoutPlans (id, name)
            SELECT id, name FROM workouts WHERE type = 'CARDIO' OR type = 1
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS exercises_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                uuid TEXT NOT NULL,
                workoutId INTEGER NOT NULL,
                FOREIGN KEY(workoutId) REFERENCES GymWorkoutPlans(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO exercises_new (id, name, description, uuid, workoutId)
            SELECT e.id, e.name, e.description, e.uuid, e.workoutId
            FROM exercises e
            JOIN workouts w ON w.id = e.workoutId
            WHERE w.type = 'GYM' OR w.type = 0
        """.trimIndent())

        db.execSQL("DROP TABLE exercises")
        db.execSQL("ALTER TABLE exercises_new RENAME TO exercises")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_exercises_workoutId ON exercises(workoutId)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS gym_sessions_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                timestamp INTEGER NOT NULL,
                workoutId INTEGER NOT NULL,
                FOREIGN KEY(workoutId) REFERENCES GymWorkoutPlans(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO gym_sessions_new (id, timestamp, workoutId)
            SELECT s.id, s.timestamp, s.workoutId
            FROM gym_sessions s
            JOIN workouts w ON w.id = s.workoutId
            WHERE w.type = 'GYM' OR w.type = 0
        """.trimIndent())

        db.execSQL("DROP TABLE gym_sessions")
        db.execSQL("ALTER TABLE gym_sessions_new RENAME TO gym_sessions")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_gym_sessions_timestamp ON gym_sessions(timestamp)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_gym_sessions_workoutId ON gym_sessions(workoutId)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS cardios_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                distance REAL,
                duration INTEGER,
                steps INTEGER,
                workoutId INTEGER NOT NULL,
                FOREIGN KEY(workoutId) REFERENCES CardioWorkoutPlans(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO cardios_new (id, distance, duration, steps, workoutId)
            SELECT c.id, c.distance, c.duration, c.steps, c.workoutId
            FROM cardios c
            JOIN workouts w ON w.id = c.workoutId
            WHERE w.type = 'CARDIO' OR w.type = 1
        """.trimIndent())

        db.execSQL("DROP TABLE cardios")
        db.execSQL("ALTER TABLE cardios_new RENAME TO cardios")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_cardios_workoutId ON cardios(workoutId)")

        db.execSQL("DROP TABLE workouts")
    }
}