package com.example.gymtracker.repository.gym

import com.example.gymtracker.utility.UnitUtil
import com.example.gymtracker.utility.UnitUtil.roundToDisplay
import com.example.gymtracker.utility.WeightUnit

fun Double.convertWeightToDatabase(): Double =
    if (UnitUtil.weightUnit == WeightUnit.KILOGRAM)
        this
    else
        UnitUtil.lbToKg(this)

fun Double.convertWeightFromDatabase(): Double =
    if (UnitUtil.weightUnit == WeightUnit.KILOGRAM)
        this
    else
        UnitUtil.kgToLb(this).roundToDisplay()