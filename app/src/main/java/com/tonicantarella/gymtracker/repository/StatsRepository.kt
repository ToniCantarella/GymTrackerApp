package com.tonicantarella.gymtracker.repository

import com.tonicantarella.gymtracker.database.dao.cardio.CardioSessionDao
import com.tonicantarella.gymtracker.database.dao.cardio.CardioWorkoutDao
import com.tonicantarella.gymtracker.database.dao.gym.ExerciseDao
import com.tonicantarella.gymtracker.database.dao.gym.GymSessionDao
import com.tonicantarella.gymtracker.database.dao.gym.GymWorkoutDao
import com.tonicantarella.gymtracker.database.dao.gym.SetDao
import com.tonicantarella.gymtracker.database.dao.gym.SetSessionDao
import com.tonicantarella.gymtracker.database.entity.cardio.CardioWorkoutEntity
import com.tonicantarella.gymtracker.database.entity.gym.GymWorkoutEntity
import com.tonicantarella.gymtracker.ui.entity.cardio.CardioWorkoutStats
import com.tonicantarella.gymtracker.ui.entity.cardio.DistanceWithTimestamp
import com.tonicantarella.gymtracker.ui.entity.cardio.DurationWithTimestamp
import com.tonicantarella.gymtracker.ui.entity.cardio.StepsWithTimestamp
import com.tonicantarella.gymtracker.ui.entity.gym.ExerciseWithHistory
import com.tonicantarella.gymtracker.ui.entity.gym.GymWorkoutStats
import com.tonicantarella.gymtracker.ui.entity.gym.SetStats
import com.tonicantarella.gymtracker.ui.entity.statsoverview.CardioWorkoutWithGeneralStats
import com.tonicantarella.gymtracker.ui.entity.statsoverview.GymWorkoutWithGeneralStats
import com.tonicantarella.gymtracker.utility.roundToDisplay
import java.time.Duration

class StatsRepository(
    private val gymWorkoutDao: GymWorkoutDao,
    private val cardioWorkoutDao: CardioWorkoutDao,
    private val gymSessionDao: GymSessionDao,
    private val cardioSessionDao: CardioSessionDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val setSessionDao: SetSessionDao,
) {
    suspend fun getGymWorkoutStats(id: Int): GymWorkoutStats? {
        val workout = gymWorkoutDao.getById(id)
        if (workout == null) return null
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

    suspend fun getCardioWorkoutStats(workoutId: Int): CardioWorkoutStats? {
        val workout = cardioWorkoutDao.getById(workoutId)
        if (workout == null) return null

        val sessions = cardioSessionDao.getAllSessionsForCardio(workoutId)
        return CardioWorkoutStats(
            id = workoutId,
            name = workout.name,
            stepsHistory = sessions?.map {
                StepsWithTimestamp(
                    value = it.steps,
                    timestamp = it.timestamp
                )
            }.orEmpty(),
            distanceHistory = sessions?.map {
                DistanceWithTimestamp(
                    value = it.distance,
                    timestamp = it.timestamp
                )
            }.orEmpty(),
            durationHistory = sessions?.map {
                DurationWithTimestamp(
                    value = it.duration,
                    timestamp = it.timestamp
                )
            }.orEmpty()
        )
    }

    suspend fun getGymWorkoutGeneralStats(workout: GymWorkoutEntity): GymWorkoutWithGeneralStats {
        val exerciseCount = exerciseDao.countExercisesByWorkoutId(workout.id)

        val avgSetStats = setSessionDao.getAverageWeightAndRepsForWorkout(workout.id)
        val avgWeight = avgSetStats?.avgWeight?.roundToDisplay() ?: 0.0
        val avgReps = avgSetStats?.avgReps?.roundToDisplay() ?: 0.0

        val avgSets = setDao.getAverageSetsPerExerciseForWorkout(workout.id) ?: 0.0

        return GymWorkoutWithGeneralStats(
            id = workout.id,
            name = workout.name,
            exerciseCount = exerciseCount,
            avgWeight = avgWeight,
            avgSets = avgSets.toInt(),
            avgReps = avgReps.toInt(),
            avgDuration = Duration.ZERO
        )
    }

    suspend fun getCardioWorkoutGeneralStats(workout: CardioWorkoutEntity): CardioWorkoutWithGeneralStats {
        val avgStats = cardioSessionDao.getAverageStatsForCardioWorkout(workout.id)

        val avgDistance = avgStats?.avgDistance?.roundToDisplay() ?: 0.0
        val avgSteps = avgStats?.avgSteps?.toInt() ?: 0
        val avgDuration = avgStats?.avgDurationMillis?.let { millis ->
            if (millis > 0) Duration.ofMillis(millis) else Duration.ZERO
        } ?: Duration.ZERO

        return CardioWorkoutWithGeneralStats(
            id = workout.id,
            name = workout.name,
            avgSteps = avgSteps,
            avgDuration = avgDuration,
            avgDistance = avgDistance,
        )
    }

    suspend fun getAllGymWorkoutsWithGeneralStats(): List<GymWorkoutWithGeneralStats> {
        return gymWorkoutDao.getAll().map { workout ->
            getGymWorkoutGeneralStats(workout)
        }
    }

    suspend fun getAllCardioWorkoutsWithGeneralStats(): List<CardioWorkoutWithGeneralStats> {
        return cardioWorkoutDao.getAll().map { workout ->
            getCardioWorkoutGeneralStats(workout)
        }
    }
}