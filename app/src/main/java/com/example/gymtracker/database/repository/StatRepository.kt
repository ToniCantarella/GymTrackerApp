package com.example.gymtracker.database.repository

import com.example.gymtracker.database.dao.WorkoutDao
import com.example.gymtracker.database.dao.cardio.CardioDao
import com.example.gymtracker.database.dao.cardio.CardioSessionDao
import com.example.gymtracker.database.dao.gym.ExerciseDao
import com.example.gymtracker.database.dao.gym.GymSessionDao
import com.example.gymtracker.database.dao.gym.SetDao
import com.example.gymtracker.database.dao.gym.SetSessionDao
import java.time.Duration
import java.time.Instant

data class SetData(
    val min: Double,
    val max: Double,
    val timestamp: Instant
)

data class ExerciseWithHistory(
    val name: String,
    val setHistory: List<SetData>
)

data class SplitStats(
    val id: Int,
    val name: String,
    val exercises: List<ExerciseWithHistory>
)

data class CardioData(
    val steps: Int?,
    val distance: Double?,
    val duration: Duration?,
    val timestamp: Instant?
)

data class CardioStats(
    val id: Int,
    val name: String,
    val cardioHistory: List<CardioData>
)

interface StatRepository {
    suspend fun getSplitStats(id: Int): SplitStats
    suspend fun getCardioStats(id: Int): CardioStats
}

class StatRepositoryImpl(
    private val workoutDao: WorkoutDao,
    private val gymSessionDao: GymSessionDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val setSessionDao: SetSessionDao,
    private val cardioDao: CardioDao,
    private val cardioSessionDao: CardioSessionDao
): StatRepository {

    override suspend fun getSplitStats(id: Int): SplitStats {
        val workout = workoutDao.getById(id)
        val exercises = exerciseDao.getExercisesByWorkoutId(workout.id)
        val gymSessions = gymSessionDao.getByWorkoutId(workout.id)

        return SplitStats(
            id = id,
            name = workout.name,
            exercises = exercises.map { exercise ->
                val setsForExercise = setDao.getSetsForExercise(exercise.id)

                ExerciseWithHistory(
                    name = exercise.name,
                    setHistory = gymSessions.map { gymSession ->
                        val setsForSession =
                            setSessionDao.getSetsForSession(gymSession.id).associateBy { it.setId }
                        val setSessionForExercise = setsForExercise.mapNotNull { set ->
                            setsForSession[set.id]
                        }
                        val min = setSessionForExercise.minByOrNull { it.weight }?.weight ?: 0.0
                        val max = setSessionForExercise.maxByOrNull { it.weight }?.weight ?: 0.0

                        SetData(
                            min = min,
                            max = max,
                            timestamp = gymSession.timestamp
                        )
                    }
                )
            }
        )
    }

    override suspend fun getCardioStats(id: Int): CardioStats {
        val workout = workoutDao.getById(id)
        val cardio = cardioDao.getCardioByWorkoutId(workout.id)
        val sessions = cardioSessionDao.getAllSessionsForCardio(cardio.id)

        return CardioStats(
            id = id,
            name = workout.name,
            cardioHistory = sessions.map {
                CardioData(
                    steps = it?.steps,
                    distance = it?.distance,
                    duration = it?.duration,
                    timestamp = it?.timestamp
                )
            }
        )
    }
}