package com.example.gymtracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.gymtracker.database.dao.ExerciseDao
import com.example.gymtracker.database.dao.SetDao
import com.example.gymtracker.database.dao.SplitDao
import com.example.gymtracker.database.dao.SplitSessionDao
import com.example.gymtracker.database.entity.workout.ExerciseEntity
import com.example.gymtracker.database.entity.workout.SetEntity
import com.example.gymtracker.database.entity.workout.SplitEntity
import com.example.gymtracker.database.entity.workout.SplitSessionEntity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.Instant
import java.util.UUID

@Database(
    entities = [
        SplitEntity::class,
        ExerciseEntity::class,
        SetEntity::class,
        SplitSessionEntity::class
    ],
    version = 3
)
@TypeConverters(Converters::class)
abstract class GymDatabase : RoomDatabase() {
    abstract fun splitDao(): SplitDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun setDao(): SetDao
    abstract fun sessionDao(): SplitSessionDao
}

class Converters {
    private val json = Json {
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(UUID::class, UUIDSerializer)
        }
    }

    @TypeConverter
    fun fromExerciseList(value: List<ExerciseEntity>): String = json.encodeToString(value)

    @TypeConverter
    fun toExerciseList(value: String): List<ExerciseEntity> = json.decodeFromString(value)

    @TypeConverter
    fun fromUUID(uuid: UUID): String = uuid.toString()

    @TypeConverter
    fun toUUID(uuid: String): UUID = UUID.fromString(uuid)

    @TypeConverter
    fun fromInstant(instant: Instant): Long = instant.toEpochMilli()

    @TypeConverter
    fun toInstant(millis: Long): Instant = Instant.ofEpochMilli(millis)
}

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}