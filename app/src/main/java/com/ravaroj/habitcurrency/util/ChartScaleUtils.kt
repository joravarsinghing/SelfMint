package com.ravaroj.habitcurrency.util

import kotlin.math.*

data class ChartScale(
    val step: Int,
    val maxValue: Int,
    val gridValues: List<Int>
)

fun calculateNiceScale(maxValue: Int, targetLines: Int = 5): ChartScale {
    if (maxValue <= 0) {
        return ChartScale(
            step = 1,
            maxValue = 5,
            gridValues = listOf(0, 1, 2, 3, 4, 5)
        )
    }

    val roughStep = maxValue.toDouble() / targetLines

    val exponent = floor(log10(roughStep))
    val base = 10.0.pow(exponent)

    val fraction = roughStep / base

    val niceFraction = when {
        fraction <= 1 -> 1.0
        fraction <= 2 -> 2.0
        fraction <= 5 -> 5.0
        else -> 10.0
    }

    val step = (niceFraction * base).toInt().coerceAtLeast(1)

    val chartMax = ceil(maxValue.toDouble() / step).toInt() * step

    val gridValues = generateSequence(0) { it + step }
        .takeWhile { it <= chartMax }
        .toList()

    return ChartScale(
        step = step,
        maxValue = chartMax,
        gridValues = gridValues
    )
}
