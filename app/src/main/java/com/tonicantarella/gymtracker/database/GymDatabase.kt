package com.tonicantarella.gymtracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
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
        CardioSessionEntity::class
    ],
    version = 1,
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
    fun fromDuration(duration: Duration): Long = duration.toMillis()

    @TypeConverter
    fun toDuration(millis: Long): Duration = Duration.ofMillis(millis)
}