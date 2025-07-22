package com.example.gymtracker.utility

import com.example.gymtracker.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

enum class WeightUnit {
    KILOGRAM,
    POUND
}

enum class DistanceUnit {
    KILOMETER,
    MILE
}

object UnitUtil {
    val weightUnit: WeightUnit
    val distanceUnit: DistanceUnit

    val weightUnitStringId: Int
    val distanceUnitStringId: Int

    init {
        val country = Locale.getDefault().country.uppercase()
        val isImperial = country in setOf("US", "LR", "MM")

        if (isImperial) {
            weightUnit = WeightUnit.POUND
            weightUnitStringId = R.string.lb

            distanceUnit = DistanceUnit.MILE
            distanceUnitStringId = R.string.mi
        }else {
            weightUnit = WeightUnit.KILOGRAM
            weightUnitStringId = R.string.kg

            distanceUnit = DistanceUnit.KILOMETER
            distanceUnitStringId = R.string.km
        }
    }

    fun kgToLb(kg: Double): Double = (kg * 2.20462).roundTo(2)
    fun lbToKg(lb: Double): Double = (lb / 2.20462).roundTo(2)

    fun kmToMi(km: Double): Double = (km * 0.621371).roundTo(1)
    fun miToKm(mi: Double): Double = (mi / 0.621371).roundTo(1)

    private fun Double.roundTo(decimals: Int): Double {
        return BigDecimal(this).setScale(decimals, RoundingMode.HALF_UP).toDouble()
    }
}