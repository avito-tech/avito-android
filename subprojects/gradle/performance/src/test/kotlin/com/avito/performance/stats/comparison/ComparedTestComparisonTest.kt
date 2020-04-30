package com.avito.performance.stats.comparison

import com.avito.performance.failed
import com.avito.performance.performedMuchBetterThanUsual
import com.avito.performance.stats.comparison.ComparedTest.Comparison
import com.avito.performance.stats.comparison.ComparedTest.Series
import com.avito.performance.stats.comparison.ComparedTest.State
import com.avito.performance.stats.comparison.ComparedTest.State.GREATER
import com.avito.performance.stats.comparison.ComparedTest.State.LESS
import com.avito.performance.stats.comparison.ComparedTest.State.SAME
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ComparedTestComparisonTest {

    @Test
    fun `performedMuchBetterThanUsual - returns nothing - if same`() {
        assertThat(
            givenTestComparison(currentSampleIs = SAME).performedMuchBetterThanUsual()
        ).isEmpty()
    }

    @Test
    fun `performedMuchBetterThanUsual - returns nothing - if less and mean diff less than threshold`() {
        assertThat(
            givenTestComparison(
                currentSampleIs = LESS,
                meanDiff = 0.0,
                metric = "FPS"
            ).performedMuchBetterThanUsual())
            .isEmpty()
    }

    @Test
    fun `performedMuchBetterThanUsual - returns nothing - if greater and mean diff less than threshold`() {
        assertThat(
            givenTestComparison(
                currentSampleIs = GREATER,
                meanDiff = 0.0,
                metric = "AvitoStartupTime"
            ).performedMuchBetterThanUsual())
            .isEmpty()
    }

    @Test
    fun `performedMuchBetterThanUsual - returns 1 - if greater and should be greater and greater than threshold`() {
        assertThat(
            givenTestComparison(
                currentSampleIs = GREATER,
                meanDiff = Double.MAX_VALUE,
                metric = "FPS"
            ).performedMuchBetterThanUsual())
            .hasSize(1)
    }

    @Test
    fun `performedMuchBetterThanUsual - returns nothing - if is not blocker`() {
        assertThat(
            givenTestComparison(
                currentSampleIs = LESS,
                meanDiff = Double.MAX_VALUE,
                metric = "ProperFramesPercent"
            ).performedMuchBetterThanUsual())
            .isEmpty()
    }

    @Test
    fun `failed - returns nothing - if same`() {
        assertThat(givenTestComparison(currentSampleIs = SAME).failed()).isEmpty()
    }

    @Test
    fun `failed - returns nothing - if less and mean diff less than threshold `() {
        assertThat(
            givenTestComparison(
                currentSampleIs = LESS,
                meanDiff = 0.0,
                metric = "FPS"
            ).failed())
            .isEmpty()
    }

    @Test
    fun `failed - returns nothing - if greater and mean diff less than threshold `() {
        assertThat(
            givenTestComparison(
                currentSampleIs = GREATER,
                meanDiff = 0.0,
                metric = "AvitoStartupTime"
            ).failed())
            .isEmpty()
    }

    @Test
    fun `failed - returns nothing - if less and should be less`() {
        assertThat(
            givenTestComparison(
                currentSampleIs = LESS,
                meanDiff = Double.MAX_VALUE,
                metric = "AvitoStartupTime"
            ).failed())
            .isEmpty()
    }

    @Test
    fun `failed - returns nothing - if greater and should be greater`() {
        assertThat(
            givenTestComparison(
                currentSampleIs = GREATER,
                meanDiff = Double.MAX_VALUE,
                metric = "FPS"
            ).failed())
            .isEmpty()
    }

    @Test
    fun `failed - returns nothing - if is not blocker`() {
        assertThat(
            givenTestComparison(
                currentSampleIs = LESS,
                meanDiff = Double.MAX_VALUE,
                metric = "ProperFramesPercent"
            ).failed())
            .isEmpty()
    }

    @Test
    fun `failed - returns 1 - if less and should be greater and greater than threshold`() {
        assertThat(
            givenTestComparison(
                currentSampleIs = LESS,
                meanDiff = Double.MAX_VALUE,
                metric = "FPS"
            ).failed())
            .hasSize(1)
    }

    @Test
    fun `failed - returns 0 - if less than zero and should be greater and less than static threshold`() {
        assertThat(
            givenTestComparison(
                currentSampleIs = LESS,
                meanDiff = -1.0,
                metric = "FPS",
                thresholdStatic = 2.0
            ).failed())
            .hasSize(0)
    }

    @Test
    fun `failed - returns 0 - if greater than zero and should be greater and greater than static threshold`() {
        assertThat(
            givenTestComparison(
                currentSampleIs = GREATER,
                meanDiff = 1.0,
                metric = "FPS",
                thresholdStatic = 0.5
            ).failed())
            .hasSize(0)
    }

    @Test
    fun `failed - returns 1 - if less than zero and should be greater and greater than static threshold`() {
        assertThat(
            givenTestComparison(
                currentSampleIs = LESS,
                meanDiff = -1.0,
                metric = "FPS",
                thresholdStatic = 0.5
            ).failed())
            .hasSize(1)
    }

    @Test
    fun `failed - returns 1 - if less than zero and should be greater and greater than threshold`() {
        assertThat(
            givenTestComparison(
                currentSampleIs = LESS,
                meanDiff = -200.0,
                metric = "FPS"
            ).failed())
            .hasSize(1)
    }

    @Test
    fun `performedMuchBetterThanUsual - returns nothing - if is should not`() {
        assertThat(
            givenTestComparison(
                currentSampleIs = LESS,
                meanDiff = -2.0,
                metric = "FPS"
            ).performedMuchBetterThanUsual())
            .isEmpty()
    }

    private fun givenTestComparison(
        significance: Double = 0.05,
        currentSampleIs: State = SAME,
        statistic: Double = 0.0,
        pValue: Double = 0.05,
        //current-previous
        meanDiff: Double = 0.0,
        threshold: Double = 0.0,
        thresholdStatic: Double = 0.0,
        metric: String = "FPS"
    ): Comparison {
        return Comparison(
            "test_name", "id",
            mapOf(
                metric to Series(
                    significance,
                    currentSampleIs,
                    statistic,
                    pValue,
                    meanDiff,
                    threshold,
                    thresholdStatic
                )
            )
        )
    }
}
