package com.tonicantarella.gymtracker.utility

import java.util.Locale

fun Double.roundToDecimalPlaces(decimals: Int = 2): Double =
    "%.${decimals}f".format(Locale.US, this).toDouble()