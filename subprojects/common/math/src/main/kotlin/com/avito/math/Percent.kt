package com.avito.math

import java.text.DecimalFormat

@Suppress("MagicNumber")
public fun Int.percentOf(sum: Int): Percent = NumberPercent(toFloat() / sum * 100)

@Suppress("MagicNumber")
public fun Long.percentOf(sum: Long): Percent = NumberPercent(toFloat() / sum * 100)

/**
 * convert 0.0 - 1.0 representation to [Percent]
 */
@Suppress("MagicNumber")
public fun Double.fromZeroToOnePercent(): Percent {
    require(this in 0.0..1.0) { "Trying to convert $this to percents; should be in [0.0..1.0] range" }
    return NumberPercent(this * 100)
}

/**
 * convert 0.0 - 1.0 representation to [Percent]
 */
@Suppress("MagicNumber")
public fun Float.fromZeroToOnePercent(): Percent {
    require(this in 0.0..1.0) { "Trying to convert $this to percents; should be in [0.0..1.0] range" }
    return NumberPercent(this * 100)
}

public fun Int.fromZeroToHundredPercent(): Percent {
    require(this in 0..100) { "Trying to convert $this to percents; should be in [0..100] range" }
    return NumberPercent(this)
}

public interface Percent {

    public fun twoDigitsString(): String

    public fun toInt(): Int

    public fun toLong(): Long
}

internal data class NumberPercent(val value: Number) : Percent {

    private val decimalFormat = DecimalFormat("0.##'%'")

    override fun twoDigitsString(): String = decimalFormat.format(value)

    override fun toInt(): Int = value.toInt()

    override fun toLong(): Long = value.toLong()

    override fun toString(): String = twoDigitsString()
}
