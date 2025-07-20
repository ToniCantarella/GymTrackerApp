package com.example.gymtracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.gymtracker.database.dao.cardio.CardioDao
import com.example.gymtracker.database.dao.cardio.CardioSessionDao
import com.example.gymtracker.database.dao.gym.ExerciseDao
import com.example.gymtracker.database.dao.gym.SetDao
import com.example.gymtracker.database.dao.gym.SetSessionDao
import com.example.gymtracker.database.dao.gym.SplitDao
import com.example.gymtracker.database.dao.gym.SplitSessionDao
import com.example.gymtracker.database.entity.cardio.CardioEntity
import com.example.gymtracker.database.entity.cardio.CardioSessionEntity
import com.example.gymtracker.database.entity.gym.ExerciseEntity
import com.example.gymtracker.database.entity.gym.SetEntity
import com.example.gymtracker.database.entity.gym.SetSessionEntity
import com.example.gymtracker.database.entity.gym.SplitEntity
import com.example.gymtracker.database.entity.gym.SplitSessionEntity
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Database(
    entities = [
        SplitEntity::class,
        ExerciseEntity::class,
        SetEntity::class,
        SetSessionEntity::class,
        SplitSessionEntity::class,
        CardioEntity::class,
        CardioSessionEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class GymDatabase : RoomDatabase() {
    abstract fun splitDao(): SplitDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun setDao(): SetDao
    abstract fun setSessionDao(): SetSessionDao
    abstract fun splitSessionDao(): SplitSessionDao
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