package com.example.gymtracker.database.repository

import com.example.gymtracker.database.dao.WorkoutDao
import com.example.gymtracker.database.dao.cardio.CardioDao
import com.example.gymtracker.database.dao.cardio.CardioSessionDao
import com.example.gymtracker.database.dao.gym.ExerciseDao
import com.example.gymtracker.database.dao.gym.GymSessionDao
import com.example.gymtracker.database.dao.gym.SetDao
import com.example.gymtracker.database.dao.gym.SetSessionDao
import com.example.gymtracker.database.entity.WorkoutEntity
import com.example.gymtracker.database.entity.WorkoutType
import com.example.gymtracker.database.entity.cardio.CardioEntity
import com.example.gymtracker.database.entity.cardio.CardioSessionEntity
import com.example.gymtracker.database.entity.gym.ExerciseEntity
import com.example.gymtracker.database.entity.gym.GymSessionEntity
import com.example.gymtracker.database.entity.gym.SetEntity
import com.example.gymtracker.database.entity.gym.SetSessionEntity
import com.example.gymtracker.ui.cardio.entity.Cardio
import com.example.gymtracker.ui.common.WorkoutListItem
import com.example.gymtracker.ui.workouts.entity.Exercise
import com.example.gymtracker.ui.workouts.entity.WorkoutSet
import com.example.gymtracker.utility.DistanceUnit
import com.example.gymtracker.utility.UnitUtil
import com.example.gymtracker.utility.WeightUnit
import java.time.Instant

data class LatestSplitWithExercises(
    val id: Int,
    val name: String,
    val timestamp: Instant?,
    val exercises: List<Exercise>
)

data class Workout(
    val name: String,
    val type: WorkoutType
)

data class WorkoutSession(
    val name: String,
    val timestamp: Instant,
    val type: WorkoutType
)

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val gymSessionDao: GymSessionDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val setSessionDao: SetSessionDao,
    private val cardioDao: CardioDao,
    private val cardioSessionDao: CardioSessionDao
) {
    suspend fun getSplitsWithLatestTimestamp(): List<WorkoutListItem> {
        val splits = workoutDao.getAllSplits()

        return splits.map {
            val timestamp = gymSessionDao.getLastSession(it.id)?.timestamp

            WorkoutListItem(
                id = it.id,
                name = it.name,
                latestTimestamp = timestamp
            )
        }.sortedBy { it.latestTimestamp }
    }

    suspend fun addSplitWithExercises(splitName: String, exercises: List<Exercise>) {
        val splitId = workoutDao.insert(
            WorkoutEntity(
                name = splitName.trim(),
                type = WorkoutType.GYM
            )
        ).toInt()

        exercises.forEach { exercise ->
            val exerciseId = exerciseDao.insert(
                ExerciseEntity(
                    workoutId = splitId,
                    uuid = exercise.uuid,
                    name = exercise.name.trim(),
                    description = exercise.description?.trim()
                )
            ).toInt()

            exercise.sets.forEach { set ->
                setDao.insert(
                    SetEntity(
                        exerciseId = exerciseId,
                        uuid = set.uuid,
                        weight = convertWeightToDb(set.weight),
                        repetitions = set.repetitions
                    )
                )
            }
        }
    }

    suspend fun deleteSplit(splitId: Int) = workoutDao.deleteById(splitId)

    suspend fun markSplitSessionDone(
        splitId: Int,
        splitName: String? = null,
        exercises: List<Exercise>
    ) {
        if (exercises.isEmpty()) return

        if (splitName?.isNotEmpty() == true) {
            val currentSplit = workoutDao.getById(splitId)
            if (currentSplit.name != splitName) {
                workoutDao.updateSplit(
                    currentSplit.copy(
                        name = splitName.trim()
                    )
                )
            }
        }

        val performedSets = exercises.filter { it.sets.any { set -> set.checked } }

        val sessionId = if (performedSets.isNotEmpty()) {
            gymSessionDao.insert(
                GymSessionEntity(
                    workoutId = splitId,
                    timestamp = Instant.now()
                )
            ).toInt()
        } else null

        val currentExercises = exerciseDao.getExercisesBySplitId(splitId).associateBy { it.uuid }

        val deletedExercises = currentExercises.values.filter { current ->
            exercises.none {
                it.uuid == current.uuid
            }
        }

        if (deletedExercises.isNotEmpty()) {
            exerciseDao.deleteExercises(deletedExercises)
        }

        exercises.forEach { exercise ->
            val currentExercise = currentExercises[exercise.uuid]
            val exerciseId = currentExercise?.id ?: exerciseDao.insert(
                ExerciseEntity(
                    workoutId = splitId,
                    uuid = exercise.uuid,
                    name = exercise.name.trim(),
                    description = exercise.description?.trim()
                )
            ).toInt()

            val exerciseInfoChanged =
                (currentExercise?.name != exercise.name || currentExercise.description != exercise.description)
            if (currentExercise != null && exerciseInfoChanged) {
                exerciseDao.updateExercise(
                    currentExercise.copy(
                        name = exercise.name.trim(),
                        description = exercise.description?.trim()
                    )
                )
            }

            val currentSets = setDao.getSetsForExercise(exerciseId).associateBy { it.uuid }

            val deletedSets = currentSets.values.filter { current ->
                exercise.sets.none { it.uuid == current.uuid }
            }

            if (deletedSets.isNotEmpty()) {
                setDao.deleteSets(deletedSets)
            }

            exercise.sets.forEach { set ->
                val currentSet = currentSets[set.uuid]
                val setId = currentSet?.id ?: setDao.insert(
                    SetEntity(
                        exerciseId = exerciseId,
                        uuid = set.uuid,
                        weight = convertWeightToDb(set.weight),
                        repetitions = set.repetitions
                    )
                ).toInt()

                val setInfoChanged =
                    (currentSet?.weight != set.weight || currentSet.repetitions != set.repetitions)
                if (currentSet != null && setInfoChanged) {
                    setDao.updateSet(
                        currentSet.copy(
                            weight = convertWeightToDb(set.weight),
                            repetitions = set.repetitions
                        )
                    )
                }

                if (sessionId != null && set.checked) {
                    setSessionDao.insert(
                        SetSessionEntity(
                            setId = setId,
                            sessionId = sessionId,
                            uuid = set.uuid,
                            weight = convertWeightToDb(set.weight),
                            repetitions = set.repetitions
                        )
                    )
                }
            }
        }
    }


    suspend fun getLatestSplitWithExercises(splitId: Int): LatestSplitWithExercises? {
        val timestamp = gymSessionDao.getLastSession(splitId)?.timestamp
        val split = workoutDao.getById(splitId)

        val exercises = exerciseDao.getExercisesBySplitId(splitId)
        val exercisesGrouped = exercises.map { exercise ->
            val sets = setDao.getSetsForExercise(exercise.id)

            Exercise(
                uuid = exercise.uuid,
                name = exercise.name,
                description = exercise.description,
                sets = sets.map { set ->
                    WorkoutSet(
                        uuid = set.uuid,
                        weight = convertWeightFromDb(set.weight),
                        repetitions = set.repetitions
                    )
                }
            )
        }

        return LatestSplitWithExercises(
            id = splitId,
            name = split.name,
            timestamp = timestamp,
            exercises = exercisesGrouped
        )
    }

    suspend fun getAllWorkouts(): List<Workout> {
        val splits = workoutDao.getAllSplits()
        val cardio = workoutDao.getAllCardio()

        return splits.map {
            Workout(
                name = it.name,
                type = WorkoutType.GYM
            )
        } + cardio.map {
            Workout(
                name = it.name,
                type = WorkoutType.CARDIO
            )
        }
    }

    suspend fun getWorkoutSessionsBetweenDates(
        startDate: Instant,
        endDate: Instant
    ): List<WorkoutSession> {
        val splitSessions = getSplitSessionsBetweenDates(startDate, endDate)
        val cardioSessions = getCardioSessionsBetweenDates(startDate, endDate)
        return splitSessions + cardioSessions
    }

    suspend fun getAllWorkoutSessions(): List<WorkoutSession> {
        val splitSessions = getAllSplitSessions()
        val cardioSessions = getAllCardioSessions()
        return splitSessions + cardioSessions
    }

    suspend fun getAllCardioSessions(): List<WorkoutSession> {
        val sessions = cardioSessionDao.getAllSessions().filterNotNull()
        return getWorkoutSessionsForCardioSessions(sessions)
    }

    suspend fun getCardioSessionsBetweenDates(
        startDate: Instant,
        endDate: Instant
    ): List<WorkoutSession> {
        val sessions = cardioSessionDao.getSessionsForTimespan(startDate, endDate).filterNotNull()
        return getWorkoutSessionsForCardioSessions(sessions)
    }

    suspend fun getAllSplitSessions(): List<WorkoutSession> {
        val sessions = gymSessionDao.getAllSessions().filterNotNull()
        return getWorkoutSessionsForSplitSessions(sessions)
    }

    suspend fun getSplitSessionsBetweenDates(
        startDate: Instant,
        endDate: Instant
    ): List<WorkoutSession> {
        val sessions = gymSessionDao.getSessionsForTimespan(startDate, endDate).filterNotNull()
        return getWorkoutSessionsForSplitSessions(sessions)
    }


    suspend fun getLatestCardio(id: Int): Cardio {
        val workout = workoutDao.getById(id)
        val cardio = cardioDao.getCardioByWorkoutId(workout.id)
        val stepSession = cardioSessionDao.getLastStepSession(id)
        val distanceSession = cardioSessionDao.getLastDistanceSession(id)
        val durationSession = cardioSessionDao.getLastDurationSession(id)
        val lastSession = cardioSessionDao.getLastSession(id)

        return Cardio(
            name = workout.name,
            steps = cardio.steps,
            stepsTimestamp = stepSession?.timestamp,
            distance = if (cardio.distance != null) convertDistanceFromDb(cardio.distance) else null,
            distanceTimestamp = distanceSession?.timestamp,
            duration = cardio.duration,
            durationTimestamp = durationSession?.timestamp,
            latestTimestamp = lastSession?.timestamp
        )
    }

    suspend fun getCardioListWithLatestTimestamp(): List<WorkoutListItem> {
        val cardioList = workoutDao.getAllCardio()

        return cardioList.map {
            val session = cardioSessionDao.getLastSession(it.id)
            WorkoutListItem(
                id = it.id,
                name = it.name,
                latestTimestamp = session?.timestamp
            )
        }
    }

    suspend fun addCardio(name: String) {
        val workoutId = workoutDao.insert(
            WorkoutEntity(
                name = name,
                type = WorkoutType.CARDIO
            )
        ).toInt()

        cardioDao.insert(
            CardioEntity(
                workoutId = workoutId
            )
        )
    }

    suspend fun deleteCardio(cardioId: Int) = cardioDao.deleteById(cardioId)

    suspend fun markCardioSessionDone(id: Int, cardio: Cardio) {
        val workout = workoutDao.getById(id)
        val cardioToUpdate = cardioDao.getCardioByWorkoutId(workout.id)
        val timestamp = Instant.now()
        val distance = cardio.distance ?: cardioToUpdate.distance
        val distanceToUpdate = if (distance != null) convertDistanceToDb(distance) else null

        if (cardio.name.isNotEmpty() && cardio.name != workout.name) {
            workoutDao.updateSplit(
                workout.copy(
                    name = cardio.name
                )
            )
        }

        cardioDao.updateCardio(
            cardioToUpdate.copy(
                steps = cardio.steps ?: cardioToUpdate.steps,
                distance = distanceToUpdate,
                duration = cardio.duration ?: cardioToUpdate.duration
            )
        )
        cardioSessionDao.insert(
            CardioSessionEntity(
                cardioId = id,
                timestamp = timestamp,
                steps = cardio.steps,
                distance = cardio.distance,
                duration = cardio.duration
            )
        )
    }

    private suspend fun getWorkoutSessionsForSplitSessions(sessions: List<GymSessionEntity>): List<WorkoutSession> {
        if (sessions.isEmpty()) return emptyList()

        val splits = workoutDao.getAllSplits().associateBy { it.id }

        return sessions.mapNotNull { session ->
            val splitName = splits[session.workoutId]?.name
            if (splitName != null) {
                WorkoutSession(
                    name = splitName,
                    timestamp = session.timestamp,
                    type = WorkoutType.GYM
                )
            } else null
        }
    }

    private suspend fun getWorkoutSessionsForCardioSessions(sessions: List<CardioSessionEntity>): List<WorkoutSession> {
        if (sessions.isEmpty()) return emptyList()

        val cardioList = workoutDao.getAllCardio().associateBy { it.id }

        return sessions.mapNotNull { session ->
            val cardioName = cardioList[session.cardioId]?.name
            if (cardioName != null) {
                WorkoutSession(
                    name = cardioName,
                    timestamp = session.timestamp,
                    type = WorkoutType.CARDIO
                )
            } else null
        }
    }

    private fun convertWeightToDb(weight: Double): Double =
        if (UnitUtil.weightUnit == WeightUnit.KILOGRAM)
            weight
        else
            UnitUtil.lbToKg(weight)


    private fun convertWeightFromDb(weight: Double): Double =
        if (UnitUtil.weightUnit == WeightUnit.KILOGRAM)
            weight
        else
            UnitUtil.kgToLb(weight)

    private fun convertDistanceToDb(distance: Double): Double =
        if (UnitUtil.distanceUnit == DistanceUnit.KILOMETER)
            distance
        else
            UnitUtil.miToKm(distance)

    private fun convertDistanceFromDb(distance: Double): Double =
        if (UnitUtil.distanceUnit == DistanceUnit.KILOMETER)
            distance
        else
            UnitUtil.kmToMi(distance)
}