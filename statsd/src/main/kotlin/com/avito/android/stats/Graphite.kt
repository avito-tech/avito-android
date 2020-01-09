package com.avito.android.stats

/**
 * https://graphite.readthedocs.io/en/latest/terminology.html#term-value
 */
fun graphiteSeriesElement(value: String) = value.replace(invalidSymbols, "_")

/**
 * https://graphite.readthedocs.io/en/latest/terminology.html#term-series
 */
fun graphiteSeries(vararg element: String) = element.joinToString(
    separator = ".",
    transform = ::graphiteSeriesElement
)

private val invalidSymbols by lazy {
    "\\W+".toRegex()
}
