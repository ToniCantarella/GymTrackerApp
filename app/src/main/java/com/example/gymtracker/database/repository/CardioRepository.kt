package com.example.gymtracker.database.repository

import com.example.gymtracker.database.dao.WorkoutDao
import com.example.gymtracker.database.dao.cardio.CardioDao
import com.example.gymtracker.database.dao.cardio.CardioSessionDao
import com.example.gymtracker.database.entity.WorkoutEntity
import com.example.gymtracker.database.entity.WorkoutType
import com.example.gymtracker.database.entity.cardio.CardioEntity
import com.example.gymtracker.database.entity.cardio.CardioSessionEntity
import com.example.gymtracker.ui.cardio.entity.Cardio
import com.example.gymtracker.utility.DistanceUnit
import com.example.gymtracker.utility.UnitUtil
import com.example.gymtracker.utility.UnitUtil.roundToDisplay
import java.time.Instant

interface CardioRepository {
    suspend fun addCardio(name: String)
    suspend fun getCardioListWithLatestTimestamp(): List<WorkoutWithLatestTimestamp>
    suspend fun getLatestCardio(id: Int): Cardio
    suspend fun getCardioBySession(sessionId: Int): Cardio
    suspend fun deleteCardio(cardioId: Int)
    suspend fun markCardioSessionDone(id: Int, cardio: Cardio, timestamp: Instant? = null)
}

class CardioRepositoryImpl(
    private val workoutDao: WorkoutDao,
    private val cardioDao: CardioDao,
    private val cardioSessionDao: CardioSessionDao
) : CardioRepository {

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

    override suspend fun getCardioListWithLatestTimestamp(): List<WorkoutWithLatestTimestamp> {
        val workouts = workoutDao.getAllCardioWorkouts()

        return workouts.map {
            val cardio = cardioDao.getCardioByWorkoutId(it.id)
            val session = cardioSessionDao.getLastSession(cardio.id)

            WorkoutWithLatestTimestamp(
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

        val stepSession = sessions.firstOrNull { it?.steps != null }
        val distanceSession = sessions.firstOrNull { it?.distance != null }
        val durationSession = sessions.firstOrNull { it?.duration != null }
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


    override suspend fun markCardioSessionDone(id: Int, cardio: Cardio, timestamp: Instant?) {
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
                timestamp = timestamp ?: Instant.now(),
                steps = cardio.steps,
                distance = cardio.distance,
                duration = cardio.duration
            )
        )
    }

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
}