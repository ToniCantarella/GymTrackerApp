package com.tonicantarella.gymtracker.repository.gym

import com.tonicantarella.gymtracker.database.dao.gym.ExerciseDao
import com.tonicantarella.gymtracker.database.dao.gym.GymSessionDao
import com.tonicantarella.gymtracker.database.dao.gym.GymWorkoutDao
import com.tonicantarella.gymtracker.database.dao.gym.SetDao
import com.tonicantarella.gymtracker.database.dao.gym.SetSessionDao
import com.tonicantarella.gymtracker.ui.entity.gym.ExerciseWithHistory
import com.tonicantarella.gymtracker.ui.entity.gym.GymWorkoutStats
import com.tonicantarella.gymtracker.ui.entity.gym.SetStats

interface GymStatsRepository {
    suspend fun getWorkoutStats(id: Int): GymWorkoutStats?
}

class GymStatsRepositoryImpl(
    private val workoutDao: GymWorkoutDao,
    private val workoutSessionDao: GymSessionDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val setSessionDao: SetSessionDao
) : GymStatsRepository {

    override suspend fun getWorkoutStats(id: Int): GymWorkoutStats? {
        val workout = workoutDao.getById(id)
        if (workout == null) return null
        val exercises = exerciseDao.getExercisesByWorkoutId(workout.id)
        val gymSessions = workoutSessionDao.getByWorkoutId(workout.id)

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
                        val minRepetitions =
                            setSessionForExercise.minByOrNull { it.repetitions }?.repetitions ?: 0
                        val maxRepetitions =
                            setSessionForExercise.maxByOrNull { it.repetitions }?.repetitions ?: 0

                        val finalMinWeight = minWeight ?: lastKnownMinWeight
                        val finalMaxWeight = maxWeight ?: lastKnownMaxWeight

                        if (minWeight != null) lastKnownMinWeight = minWeight
                        if (maxWeight != null) lastKnownMaxWeight = maxWeight

                        SetStats(
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
}