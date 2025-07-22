package com.example.gymtracker.database.repository

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

enum class WorkoutType {
    GYM,
    CARDIO
}

data class WorkoutSession(
    val name: String,
    val timestamp: Instant,
    val type: WorkoutType
)

class WorkoutRepository(
    private val splitDao: SplitDao,
    private val splitSessionDao: SplitSessionDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val setSessionDao: SetSessionDao,
    private val cardioDao: CardioDao,
    private val cardioSessionDao: CardioSessionDao
) {
    suspend fun getSplitsWithLatestTimestamp(): List<WorkoutListItem> {
        val splits = splitDao.getAllSplits()

        return splits.map {
            val timestamp = splitSessionDao.getLastSession(it.id)?.timestamp

            WorkoutListItem(
                id = it.id,
                name = it.name,
                latestTimestamp = timestamp
            )
        }.sortedBy { it.latestTimestamp }
    }

    suspend fun addSplitWithExercises(splitName: String, exercises: List<Exercise>) {
        val splitId = splitDao.insert(SplitEntity(name = splitName.trim())).toInt()

        exercises.forEach { exercise ->
            val exerciseId = exerciseDao.insert(
                ExerciseEntity(
                    splitId = splitId,
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

    suspend fun deleteSplit(splitId: Int) = splitDao.deleteById(splitId)

    suspend fun markSplitSessionDone(
        splitId: Int,
        splitName: String? = null,
        exercises: List<Exercise>
    ) {
        if (exercises.isEmpty()) return

        if (splitName?.isNotEmpty() == true) {
            val currentSplit = splitDao.getSplitById(splitId)
            if (currentSplit.name != splitName) {
                splitDao.updateSplit(
                    currentSplit.copy(
                        name = splitName.trim()
                    )
                )
            }
        }

        val performedSets = exercises.filter { it.sets.any { set -> set.checked } }

        val sessionId = if (performedSets.isNotEmpty()) {
            splitSessionDao.insert(
                SplitSessionEntity(
                    splitId = splitId,
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
                    splitId = splitId,
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
        val timestamp = splitSessionDao.getLastSession(splitId)?.timestamp
        val split = splitDao.getSplitById(splitId)

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
        val splits = splitDao.getAllSplits()
        val cardio = cardioDao.getAllCardio()

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
        val sessions = splitSessionDao.getAllSessions().filterNotNull()
        return getWorkoutSessionsForSplitSessions(sessions)
    }

    suspend fun getSplitSessionsBetweenDates(
        startDate: Instant,
        endDate: Instant
    ): List<WorkoutSession> {
        val sessions = splitSessionDao.getSessionsForTimespan(startDate, endDate).filterNotNull()
        return getWorkoutSessionsForSplitSessions(sessions)
    }


    suspend fun getLatestCardio(id: Int): Cardio {
        val cardio = cardioDao.getCardioById(id)

        return Cardio(
            name = cardio.name,
            steps = cardio.steps,
            stepsTimestamp = cardio.stepsTimestamp,
            distance = if (cardio.distance != null) convertDistanceFromDb(cardio.distance) else null,
            distanceTimestamp = cardio.distanceTimestamp,
            duration = cardio.duration,
            durationTimestamp = cardio.durationTimestamp,
            latestTimestamp = cardio.latestTimestamp
        )
    }

    suspend fun getCardioListWithLatestTimestamp(): List<WorkoutListItem> {
        val cardioList = cardioDao.getAllCardio()
        return cardioList.map {
            WorkoutListItem(
                id = it.id,
                name = it.name,
                latestTimestamp = it.latestTimestamp
            )
        }
    }

    suspend fun addCardio(name: String) {
        cardioDao.insert(
            CardioEntity(
                name = name
            )
        )
    }

    suspend fun deleteCardio(cardioId: Int) = cardioDao.deleteById(cardioId)

    suspend fun markCardioSessionDone(id: Int, cardio: Cardio) {
        val cardioToUpdate = cardioDao.getCardioById(id)
        val timestamp = Instant.now()
        val distance = cardio.distance ?: cardioToUpdate.distance
        val distanceToUpdate = if (distance != null) convertDistanceToDb(distance) else null
        cardioDao.updateCardio(
            cardioToUpdate.copy(
                name = if (cardio.name.isNotEmpty() && cardio.name != cardioToUpdate.name) cardio.name else cardioToUpdate.name,
                steps = cardio.steps ?: cardioToUpdate.steps,
                stepsTimestamp = if (cardio.steps != null) timestamp else cardioToUpdate.stepsTimestamp,
                distance = distanceToUpdate,
                distanceTimestamp = if (cardio.distance != null) timestamp else cardioToUpdate.distanceTimestamp,
                duration = cardio.duration ?: cardioToUpdate.duration,
                durationTimestamp = if (cardio.duration != null) timestamp else cardioToUpdate.durationTimestamp,
                latestTimestamp = timestamp
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

    private suspend fun getWorkoutSessionsForSplitSessions(sessions: List<SplitSessionEntity>): List<WorkoutSession> {
        if (sessions.isEmpty()) return emptyList()

        val splits = splitDao.getAllSplits().associateBy { it.id }

        return sessions.mapNotNull { session ->
            val splitName = splits[session.splitId]?.name
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

        val cardioList = cardioDao.getAllCardio().associateBy { it.id }

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