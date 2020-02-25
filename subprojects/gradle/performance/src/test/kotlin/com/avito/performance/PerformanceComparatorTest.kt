package com.avito.performance

import com.avito.performance.stats.Stats
import com.avito.performance.stats.compare.TestForComparing
import com.avito.performance.stats.comparison.ComparedTest
import com.avito.report.model.PerformanceTest
import com.google.common.truth.Truth
import org.funktionale.tries.Try
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class PerformanceComparatorTest {

    lateinit var comparator: PerformanceTestComparator
    lateinit var outputFile: File
    lateinit var stats: Stats


    @BeforeEach
    fun setup(@TempDir temp: Path) {
        stats = givenStats()
        comparator = PerformanceTestComparator(stats)
        outputFile = File(temp.toFile(), "compare.txt")
    }

    @Test
    fun `collect - generates full file - when get results with same count`() {
        val prev = givenPerfTests(count = 3, gap = 1)
        val current = givenPerfTests(count = 3, gap = 2)

        compare(current, prev)

        Assertions.assertTrue(outputFile.exists()) { "current-runs.txt should exist!" }

        Truth.assertThat(outputFile.readText()).contains(
            ("""[{"testName":"testname0\"","id":"0","series":{"testname0\"_fps":{"significance":0.0,"currentSampleIs":
                |"same","statistic":0.0,"pValue":1.0,"meanDiff":2.0,"threshold":100.0,"thresholdStatic":0.0}}},
                |{"testName":"testname1\"","id":"1","series":{"testname1\"_fps":{"significance":0.0,"currentSampleIs":
                |"same","statistic":0.0,"pValue":1.0,"meanDiff":2.0,"threshold":100.0,"thresholdStatic":0.0}}},
                |{"testName":"testname2\"","id":"2","series":{"testname2\"_fps":{"significance":0.0,"currentSampleIs":
                |"same","statistic":0.0,"pValue":1.0,"meanDiff":2.0,"threshold":100.0,"thresholdStatic":0.0}}},
                |{"testName":"testname3\"","id":"3","series":{"testname3\"_fps":{"significance":0.0,"currentSampleIs":
                |"same","statistic":0.0,"pValue":1.0,"meanDiff":2.0,"threshold":100.0,"thresholdStatic":0.0}}}]"""
                .trimMargin())
                .trimIndent()
                .replace("\n", "")
        )
    }

    @Test
    fun `collect - generates full file - when get results with different count`() {
        val prev = givenPerfTests(count = 1, gap = 2)
        val current = givenPerfTests(count = 3, gap = 4)

        compare(current, prev)

        Assertions.assertTrue(outputFile.exists()) { "current-runs.txt should exist" }

        Truth.assertThat(outputFile.readText()).contains(
            ("""[{"testName":"testname0\"","id":"0","series":{"testname0\"_fps":{"significance":0.0,"currentSampleIs"
                |:"same","statistic":0.0,"pValue":1.0,"meanDiff":2.0,"threshold":100.0,"thresholdStatic":0.0}}},
                |{"testName":"testname1\"","id":"1","series":{"testname1\"_fps":{"significance":0.0,"currentSampleIs":
                |"same","statistic":0.0,"pValue":1.0,"meanDiff":2.0,"threshold":100.0,"thresholdStatic":0.0}}}]"""
                .trimMargin())
                .trimIndent()
                .replace("\n", "")
        )
    }

    private fun compare(
        current: List<PerformanceTest>,
        prev: List<PerformanceTest>
    ) {
        val list = comparator.compare(current, prev)
        PerformanceWriter().write(list, outputFile)
    }

    private fun givenPerfTests(count: Int, gap: Int, deep: Int = 0): List<PerformanceTest> {
        val tests = mutableListOf<PerformanceTest>()

        (0..count).forEach {
            val testName = "testname$it\""
            val series = mutableMapOf<String, List<Double>>()
            repeat((0..deep).count()) {
                series["${testName}_fps"] = listOf(0.0 + gap)
            }
            val test = PerformanceTest(testName, it.toString(), series)
            tests.add(test)
        }

        return tests
    }

    private fun givenStats(): Stats = object : Stats {
        override fun mde(perfTests: List<PerformanceTest>) {

        }

        override fun compare(toCompare: List<TestForComparing>): Try<List<ComparedTest.Result>> {
            return Try.Success(toCompare
                .map {
                    ComparedTest.Result(
                        it.testName,
                        series = it.series.map {
                            it.key to ComparedTest.Series(
                                0.0,
                                ComparedTest.State.SAME,
                                0.0,
                                1.0,
                                2.0,
                                threshold = 100.0,
                                thresholdStatic = 0.0
                            )
                        }.toMap()
                    )
                }
                .toList())
        }
    }
}
