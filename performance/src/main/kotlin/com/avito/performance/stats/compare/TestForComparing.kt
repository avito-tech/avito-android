package com.avito.performance.stats.compare

internal data class TestForComparing(
    val testName: String,
    val series: Map<String, Series>
) {

    data class Series(
        val previous: List<Double>,
        val current: List<Double>,
        val significance: Double
    )
}
