package com.pontolivre.shared.util

import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.abs

/**
 * Simple format extension for String to be used in commonMain.
 * Supports "%.2f".format(value) pattern.
 */
fun String.format(value: Double): String {
    return if (this == "%.2f") {
        value.toFormattedString(2)
    } else if (this.contains("%.2f")) {
        this.replace("%.2f", value.toFormattedString(2))
    } else {
        this
    }
}

/**
 * Helper to format double to string with fixed decimal places.
 */
fun Double.toFormattedString(decimals: Int): String {
    val isNegative = this < 0
    val factor = 10.0.pow(decimals)
    val rounded = (abs(this) * factor).roundToLong()
    val str = rounded.toString().padStart(decimals + 1, '0')
    val splitIndex = str.length - decimals
    val result = str.substring(0, splitIndex) + "." + str.substring(splitIndex)
    val final = if (result.startsWith("0") && result.length > decimals + 1 && !result.startsWith("0.")) {
        result.substring(1) // remove leading zero if not 0.xx
    } else {
        result
    }
    return if (isNegative) "-$final" else final
}
