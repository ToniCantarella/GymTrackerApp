package com.example.gymtracker.database.repository

import com.example.gymtracker.database.GymDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

data class SplitWithExercises(
    val id: Int,
    val name: String,
    val timestamp: Instant?,
    val exercises: List<Exercise>
)

data class Workout(
    val id: Int,
    val name: String,
    val type: WorkoutType
)

data class WorkoutSession(
    val id: Int,
    val workout: Workout,
    val timestamp: Instant,
)

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

interface WorkoutRepository {
    suspend fun addSplitWithExercises(splitName: String, exercises: List<Exercise>)
    suspend fun getSplitsWithLatestTimestamp(): List<WorkoutListItem>
    suspend fun getLatestSplitWithExercises(id: Int): SplitWithExercises?
    suspend fun getSplitBySession(sessionId: Int): SplitWithExercises
    suspend fun deleteSplit(splitId: Int)
    suspend fun markSplitSessionDone(
        splitId: Int,
        splitName: String,
        exercises: List<Exercise>
    )

    suspend fun getSplitStats(id: Int): SplitStats

    suspend fun addCardio(name: String)
    suspend fun getCardioListWithLatestTimestamp(): List<WorkoutListItem>
    suspend fun getLatestCardio(id: Int): Cardio
    suspend fun getCardioBySession(sessionId: Int): Cardio
    suspend fun deleteCardio(cardioId: Int)
    suspend fun markCardioSessionDone(id: Int, cardio: Cardio)
    suspend fun getCardioStats(id: Int): CardioStats
    suspend fun getAllWorkouts(): List<Workout>
    suspend fun getAllWorkoutSessions(): List<WorkoutSession>
    suspend fun getWorkoutSessionsBetweenDates(
        startDate: Instant,
        endDate: Instant
    ): List<WorkoutSession>

    suspend fun deleteAllData()
}

class WorkoutRepositoryImpl(
    private val db: GymDatabase,
    private val workoutDao: WorkoutDao,
    private val gymSessionDao: GymSessionDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val setSessionDao: SetSessionDao,
    private val cardioDao: CardioDao,
    private val cardioSessionDao: CardioSessionDao
) : WorkoutRepository {

    override suspend fun addSplitWithExercises(splitName: String, exercises: List<Exercise>) {
        val workoutId = workoutDao.insert(
            WorkoutEntity(
                name = splitName.trim(),
                type = WorkoutType.GYM
            )
        ).toInt()

        exercises.forEach { exercise ->
            val exerciseId = exerciseDao.insert(
                ExerciseEntity(
                    workoutId = workoutId,
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
                        weight = convertWeightToDatabase(set.weight),
                        repetitions = set.repetitions
                    )
                )
            }
        }
    }

    override suspend fun getSplitsWithLatestTimestamp(): List<WorkoutListItem> {
        val workouts = workoutDao.getAllGymWorkouts()

        return workouts.map {
            val timestamp = gymSessionDao.getLastSession(it.id)?.timestamp

            WorkoutListItem(
                id = it.id,
                name = it.name,
                latestTimestamp = timestamp
            )
        }.sortedBy { it.latestTimestamp }
    }

    override suspend fun getLatestSplitWithExercises(id: Int): SplitWithExercises? {
        val workout = workoutDao.getById(id)
        val timestamp = gymSessionDao.getLastSession(id)?.timestamp

        val exercises = exerciseDao.getExercisesByWorkoutId(id)
        val exercisesGrouped = exercises.map { exercise ->
            val sets = setDao.getSetsForExercise(exercise.id)

            Exercise(
                uuid = exercise.uuid,
                name = exercise.name,
                description = exercise.description,
                sets = sets.map { set ->
                    WorkoutSet(
                        uuid = set.uuid,
                        weight = convertWeightFromDatabase(set.weight),
                        repetitions = set.repetitions
                    )
                }
            )
        }

        return SplitWithExercises(
            id = id,
            name = workout.name,
            timestamp = timestamp,
            exercises = exercisesGrouped
        )
    }

    override suspend fun getSplitBySession(
        sessionId: Int
    ): SplitWithExercises {
        val gymSession = gymSessionDao.getById(sessionId)
        val split = workoutDao.getById(gymSession.workoutId)
        val exercises = exerciseDao.getExercisesByWorkoutId(split.id)
        val setSession = setSessionDao.getSetsForSession(gymSession.id)

        return SplitWithExercises(
            id = split.id,
            name = split.name,
            timestamp = gymSession.timestamp,
            exercises = exercises.map { exercise ->
                val sets = setDao.getSetsForExercise(exercise.id)
                val setSessionsForExercise =
                    setSession.filter { sets.any { set -> it.setId == set.id } }

                Exercise(
                    uuid = exercise.uuid,
                    name = exercise.name,
                    description = exercise.description,
                    sets = setSessionsForExercise.map { set ->
                        WorkoutSet(
                            uuid = set.uuid,
                            weight = set.weight,
                            repetitions = set.repetitions
                        )
                    }
                )
            }
        )
    }

    override suspend fun deleteSplit(splitId: Int) = workoutDao.deleteById(splitId)

    override suspend fun markSplitSessionDone(
        splitId: Int,
        splitName: String,
        exercises: List<Exercise>
    ) {
        if (exercises.isEmpty()) return

        val currentSplit = workoutDao.getById(splitId)
        val newName = splitName.trim()

        if (newName.isNotEmpty() && currentSplit.name != newName) {
            workoutDao.updateWorkout(
                currentSplit.copy(
                    name = newName
                )
            )
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

        val currentExercises = exerciseDao.getExercisesByWorkoutId(splitId).associateBy { it.uuid }

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
                        weight = convertWeightToDatabase(set.weight),
                        repetitions = set.repetitions
                    )
                ).toInt()

                val weight = convertWeightToDatabase(set.weight)
                val setInfoChanged = currentSet?.weight != weight ||
                        currentSet.repetitions != set.repetitions

                if (currentSet != null && setInfoChanged) {
                    setDao.updateSet(
                        currentSet.copy(
                            weight = weight,
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
                            weight = convertWeightToDatabase(set.weight),
                            repetitions = set.repetitions
                        )
                    )
                }
            }
        }
    }

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

    override suspend fun addCardio(name: String) {
        val workoutId = workoutDao.insert(
            WorkoutEntity(
                name = name.trim(),
                type = WorkoutType.CARDIO
            )
        ).toInt()

        cardioDao.insert(
            CardioEntity(
                workoutId = workoutId
            )
        )
    }

    override suspend fun getCardioListWithLatestTimestamp(): List<WorkoutListItem> {
        val workouts = workoutDao.getAllCardioWorkouts()

        return workouts.map {
            val cardio = cardioDao.getCardioByWorkoutId(it.id)
            val session = cardioSessionDao.getLastSession(cardio.id)

            WorkoutListItem(
                id = it.id,
                name = it.name,
                latestTimestamp = session?.timestamp
            )
        }
    }

    override suspend fun getLatestCardio(id: Int): Cardio {
        val workout = workoutDao.getById(id)
        val cardio = cardioDao.getCardioByWorkoutId(workout.id)
        val sessions = cardioSessionDao.getAllSessionsForCardio(cardio.id)

        val stepSession = sessions.firstOrNull { it.steps != null }
        val distanceSession = sessions.firstOrNull { it.distance != null }
        val durationSession = sessions.firstOrNull { it.duration != null }
        val lastSession = sessions.firstOrNull()

        return Cardio(
            name = workout.name,
            steps = cardio.steps,
            stepsTimestamp = stepSession?.timestamp,
            distance = if (cardio.distance != null) convertDistanceFromDatabase(cardio.distance) else null,
            distanceTimestamp = distanceSession?.timestamp,
            duration = cardio.duration,
            durationTimestamp = durationSession?.timestamp,
            latestTimestamp = lastSession?.timestamp
        )
    }

    override suspend fun getCardioBySession(
        sessionId: Int
    ): Cardio {
        val session = cardioSessionDao.getById(sessionId)
        val cardio = cardioDao.getById(session.cardioId)
        val workout = workoutDao.getById(cardio.workoutId)

        return Cardio(
            name = workout.name,
            steps = session.steps,
            stepsTimestamp = session.timestamp,
            distance = session.distance,
            distanceTimestamp = session.timestamp,
            duration = session.duration,
            durationTimestamp = session.timestamp,
            latestTimestamp = session.timestamp
        )
    }

    override suspend fun deleteCardio(cardioId: Int) = workoutDao.deleteById(cardioId)


    override suspend fun markCardioSessionDone(id: Int, cardio: Cardio) {
        val workout = workoutDao.getById(id)
        val newName = cardio.name.trim()

        if (newName.isNotEmpty() && workout.name != newName) {
            workoutDao.updateWorkout(
                workout.copy(
                    name = newName
                )
            )
        }

        val currentCardio = cardioDao.getCardioByWorkoutId(workout.id)

        val distance =
            if (cardio.distance != null)
                convertDistanceToDatabase(cardio.distance)
            else
                null

        val stepsChanced = (cardio.steps != null && cardio.steps != currentCardio.steps)
        val distanceChanced = (distance != null && distance != currentCardio.distance)
        val durationChanced = (cardio.duration != null && cardio.duration != currentCardio.duration)
        val cardioUpdated = (stepsChanced || distanceChanced || durationChanced)

        if (cardioUpdated) {
            cardioDao.updateCardio(
                currentCardio.copy(
                    steps = cardio.steps ?: currentCardio.steps,
                    distance = distance ?: currentCardio.distance,
                    duration = cardio.duration ?: currentCardio.duration
                )
            )
        }

        cardioSessionDao.insert(
            CardioSessionEntity(
                cardioId = currentCardio.id,
                timestamp = Instant.now(),
                steps = cardio.steps,
                distance = cardio.distance,
                duration = cardio.duration
            )
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
                    steps = it.steps,
                    distance = it.distance,
                    duration = it.duration,
                    timestamp = it.timestamp
                )
            }
        )
    }

    override suspend fun getAllWorkouts(): List<Workout> {
        val workouts = workoutDao.getAllWorkouts()

        return workouts.map {
            Workout(
                id = it.id,
                name = it.name,
                type = it.type
            )
        }
    }

    override suspend fun getAllWorkoutSessions(): List<WorkoutSession> {
        val splitSessions = getAllGymWorkoutSessions()
        val cardioSessions = getAllCardioWorkoutSessions()
        return splitSessions + cardioSessions
    }

    override suspend fun getWorkoutSessionsBetweenDates(
        startDate: Instant,
        endDate: Instant
    ): List<WorkoutSession> {
        val gymSessions = getGymSessionsBetweenDates(startDate, endDate)
        val cardioSessions = getCardioSessionsBetweenDates(startDate, endDate)
        return gymSessions + cardioSessions
    }

    override suspend fun deleteAllData() {
        CoroutineScope(Dispatchers.IO).launch {
            db.clearAllTables()
        }
    }

    private suspend fun getAllGymWorkoutSessions(): List<WorkoutSession> {
        val sessions = gymSessionDao.getAllSessions().filterNotNull()
        return gymSessionsToWorkoutSessions(sessions)
    }

    private suspend fun getAllCardioWorkoutSessions(): List<WorkoutSession> {
        val sessions = cardioSessionDao.getAllSessions().filterNotNull()
        return cardioSessionsToWorkoutSessions(sessions)
    }

    private suspend fun getGymSessionsBetweenDates(
        startDate: Instant,
        endDate: Instant
    ): List<WorkoutSession> {
        val sessions = gymSessionDao.getSessionsForTimespan(startDate, endDate).filterNotNull()
        return gymSessionsToWorkoutSessions(sessions)
    }

    private suspend fun getCardioSessionsBetweenDates(
        startDate: Instant,
        endDate: Instant
    ): List<WorkoutSession> {
        val sessions = cardioSessionDao.getSessionsForTimespan(startDate, endDate).filterNotNull()
        return cardioSessionsToWorkoutSessions(sessions)
    }

    private suspend fun gymSessionsToWorkoutSessions(sessions: List<GymSessionEntity>): List<WorkoutSession> {
        if (sessions.isEmpty()) return emptyList()

        val workouts = workoutDao.getAllGymWorkouts().associateBy { it.id }

        return sessions.mapNotNull { session ->
            val split = workouts[session.workoutId]
            if (split != null) {
                WorkoutSession(
                    id = session.id,
                    workout = Workout(
                        id = split.id,
                        name = split.name,
                        type = split.type
                    ),
                    timestamp = session.timestamp,
                )
            } else null
        }
    }

    private suspend fun cardioSessionsToWorkoutSessions(sessions: List<CardioSessionEntity>): List<WorkoutSession> {
        if (sessions.isEmpty()) return emptyList()

        val workouts = workoutDao.getAllCardioWorkouts().associateBy { it.id }
        val cardioList = cardioDao.getAllCardio().associateBy { it.id }

        return sessions.mapNotNull { session ->
            val cardio = cardioList[session.cardioId]
            if (cardio != null) {
                val workout = workouts[cardio.workoutId]
                if (workout != null) {
                    WorkoutSession(
                        id = session.id,
                        workout = Workout(
                            id = workout.id,
                            name = workout.name,
                            type = workout.type
                        ),
                        timestamp = session.timestamp,
                    )
                } else null
            } else null
        }
    }

    private fun convertWeightToDatabase(weight: Double): Double =
        if (UnitUtil.weightUnit == WeightUnit.KILOGRAM)
            weight
        else
            UnitUtil.lbToKg(weight)

    private fun convertWeightFromDatabase(weight: Double): Double =
        if (UnitUtil.weightUnit == WeightUnit.KILOGRAM)
            weight
        else
            UnitUtil.kgToLb(weight).roundToDisplay()

    private fun convertDistanceToDatabase(distance: Double): Double =
        if (UnitUtil.distanceUnit == DistanceUnit.KILOMETER)
            distance
        else
            UnitUtil.miToKm(distance)

    private fun convertDistanceFromDatabase(distance: Double): Double =
        if (UnitUtil.distanceUnit == DistanceUnit.KILOMETER)
            distance
        else
            UnitUtil.kmToMi(distance).roundToDisplay()

    private fun Double.roundToDisplay(decimals: Int = 2): Double =
        "%.${decimals}f".format(this).toDouble()
}