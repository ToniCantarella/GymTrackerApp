package com.tonicantarella.gymtracker.utility

fun Double.roundToDisplay(decimals: Int = 2): Double =
    "%.${decimals}f".format(this).toDouble()