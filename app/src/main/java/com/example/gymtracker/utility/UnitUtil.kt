package com.example.gymtracker.utility

import com.example.gymtracker.R
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
    private const val WEIGHT_CONVERSION_DOUBLE = 2.2046226218
    private const val DISTANCE_CONVERSION_DOUBLE = 0.6213711922

    private val isImperial: Boolean
        get() = Locale.getDefault().country.uppercase() in setOf("US", "LR", "MM")

    val weightUnit: WeightUnit
        get() = if (isImperial) WeightUnit.POUND else WeightUnit.KILOGRAM

    val weightUnitStringId: Int
        get() = if (isImperial) R.string.lb else R.string.kg

    val distanceUnit: DistanceUnit
        get() = if (isImperial) DistanceUnit.MILE else DistanceUnit.KILOMETER

    val distanceUnitStringId: Int
        get() = if (isImperial) R.string.mi else R.string.km

    fun kgToLb(kg: Double): Double = kg * WEIGHT_CONVERSION_DOUBLE
    fun lbToKg(lb: Double): Double = lb / WEIGHT_CONVERSION_DOUBLE

    fun kmToMi(km: Double): Double = km * DISTANCE_CONVERSION_DOUBLE
    fun miToKm(mi: Double): Double = mi / DISTANCE_CONVERSION_DOUBLE
}