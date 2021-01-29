package com.avito.math

import java.text.DecimalFormat

@Suppress("MagicNumber")
fun Int.percentOf(sum: Int): Percent = NumberPercent(toFloat() / sum * 100)

@Suppress("MagicNumber")
fun Long.percentOf(sum: Long): Percent = NumberPercent(toFloat() / sum * 100)

/**
 * convert 0.0 - 1.0 representation to [Percent]
 */
@Suppress("MagicNumber")
fun Double.percent(): Percent {
    require(this in 0.0..1.0) { "Trying to convert $this to percents; should be in [0.0..1.0] range" }
    return NumberPercent(this * 100)
}

/**
 * convert 0.0 - 1.0 representation to [Percent]
 */
@Suppress("MagicNumber")
fun Float.percent(): Percent {
    require(this in 0.0..1.0) { "Trying to convert $this to percents; should be in [0.0..1.0] range" }
    return NumberPercent(this * 100)
}

interface Percent {

    fun twoDigitsString(): String

    fun toInt(): Int

    fun toLong(): Long
}

internal class NumberPercent(val value: Number) : Percent {

    private val decimalFormat = DecimalFormat("0.##'%'")

    override fun twoDigitsString(): String = decimalFormat.format(value)

    override fun toInt(): Int = value.toInt()

    override fun toLong(): Long = value.toLong()

    override fun toString(): String = twoDigitsString()
}
