package com.avito.performance.stats.comparison

import com.google.gson.annotations.SerializedName

internal interface ComparedTest {

    data class Result(
        val testName: String,
        val series: Map<String, Series>
    )

    data class Series(
        val significance: Double,
        val currentSampleIs: State,
        val statistic: Double,
        val pValue: Double,
        val meanDiff: Double, //current-previous
        val threshold: Double,
        val thresholdStatic: Double //overrides threshold for particular tests in mongo
    )

    enum class State {
        @SerializedName("same")
        SAME,

        @SerializedName("greater")
        GREATER,

        @SerializedName("less")
        LESS
    }

    data class Comparison(
        val testName: String,
        val id: String,
        val series: Map<String, Series>
    )

}
