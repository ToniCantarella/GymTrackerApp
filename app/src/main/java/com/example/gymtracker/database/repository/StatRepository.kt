package com.example.gymtracker.database.repository

import com.example.gymtracker.database.GymDatabase
import com.example.gymtracker.database.dao.WorkoutDao
import com.example.gymtracker.database.dao.cardio.CardioDao
import com.example.gymtracker.database.dao.cardio.CardioSessionDao
import com.example.gymtracker.database.dao.gym.ExerciseDao
import com.example.gymtracker.database.dao.gym.GymSessionDao
import com.example.gymtracker.database.dao.gym.SetDao
import com.example.gymtracker.database.dao.gym.SetSessionDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

data class SetData(
    val minWeight: Double,
    val maxWeight: Double,
    val minRepetitions: Int,
    val maxRepetitions: Int,
    val timestamp: Instant
)

data class ExerciseWithHistory(
    val name: String,
    val setHistory: List<SetData>
)

data class GymWorkoutStats(
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
    suspend fun getGymWorkoutStats(id: Int): GymWorkoutStats
    suspend fun getCardioStats(id: Int): CardioStats
    suspend fun deleteAllData()
}

class StatRepositoryImpl(
    private val db: GymDatabase,
    private val workoutDao: WorkoutDao,
    private val gymSessionDao: GymSessionDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val setSessionDao: SetSessionDao,
    private val cardioDao: CardioDao,
    private val cardioSessionDao: CardioSessionDao
): StatRepository {

    override suspend fun getGymWorkoutStats(id: Int): GymWorkoutStats {
        val workout = workoutDao.getById(id)
        val exercises = exerciseDao.getExercisesByWorkoutId(workout.id)
        val gymSessions = gymSessionDao.getByWorkoutId(workout.id)

        return GymWorkoutStats(
            id = id,
            name = workout.name,
            exercises = exercises.map { exercise ->
                val setsForExercise = setDao.getSetsForExercise(exercise.id)

                var lastKnownMinWeight = 0.0
                var lastKnownMaxWeight = 0.0

                ExerciseWithHistory(
                    name = exercise.name,
                    setHistory = gymSessions.map { gymSession ->
                        val setsForSession = setSessionDao
                            .getSetsForSession(gymSession.id)
                            .associateBy { it.setId }
                        val setSessionForExercise = setsForExercise.mapNotNull { set ->
                            setsForSession[set.id]
                        }

                        val minWeight = setSessionForExercise.minByOrNull { it.weight }?.weight
                        val maxWeight = setSessionForExercise.maxByOrNull { it.weight }?.weight
                        val minRepetitions = setSessionForExercise.minByOrNull { it.repetitions }?.repetitions ?: 0
                        val maxRepetitions = setSessionForExercise.maxByOrNull { it.repetitions }?.repetitions ?: 0

                        val finalMinWeight = minWeight ?: lastKnownMinWeight
                        val finalMaxWeight = maxWeight ?: lastKnownMaxWeight

                        if (minWeight != null) lastKnownMinWeight = minWeight
                        if (maxWeight != null) lastKnownMaxWeight = maxWeight

                        SetData(
                            minWeight = finalMinWeight,
                            maxWeight = finalMaxWeight,
                            minRepetitions = minRepetitions,
                            maxRepetitions = maxRepetitions,
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

    override suspend fun deleteAllData() {
        CoroutineScope(Dispatchers.IO).launch {
            db.clearAllTables()
        }
    }
}