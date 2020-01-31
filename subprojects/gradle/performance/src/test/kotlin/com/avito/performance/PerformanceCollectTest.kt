package com.avito.performance

import com.avito.report.ReportsApi
import com.avito.report.model.PerformanceTest
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.createStubInstance
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import org.funktionale.tries.Try
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class PerformanceCollectTest {

    private lateinit var collector: PerformanceTestCollector
    private lateinit var outputFile: File
    private val reports: ReportsApi = mock()
    private val id: ReportCoordinates = ReportCoordinates.createStubInstance()
    private val buildId = "123"

    @BeforeEach
    fun setup(@TempDir temp: Path) {
        collector = PerformanceTestCollector(reports, id, buildId)
        outputFile = File(temp.toFile(), "current-runs.txt")
    }

    @Test
    fun `collect - generates full file - when get results`() {
        givenTests(otherBuildTestsCount = 4, properTestsCount = 3)

        collect()

        assertTrue(outputFile.exists()) { "current-runs.txt should exist" }

        Truth.assertThat(outputFile.readText()).contains(
            ("""[{"testName":"test0","id":"id0","series":{"test0_fps":[22.0]}},
                |{"testName":"test1","id":"id1","series":{"test1_fps":[22.0]}},
                |{"testName":"test2","id":"id2","series":{"test2_fps":[22.0]}},
                |{"testName":"test3","id":"id3","series":{"test3_fps":[22.0]}}]"""
                .trimMargin())
                .trimIndent()
                .replace("\n", "")
        )
    }

    private fun givenTests(otherBuildTestsCount: Int, properTestsCount: Int) {

        val list = mutableListOf<SimpleRunTest>()
        (0..otherBuildTestsCount).forEach {
            list.add(
                SimpleRunTest.createStubInstance(
                    name = "test$it",
                    tcBuild = "321",
                    id = "id$it"
                )
            )
        }
        (0..properTestsCount).forEach {
            list.add(
                SimpleRunTest.createStubInstance(
                    name = "test$it",
                    tcBuild = buildId,
                    id = "id$it"
                )
            )
        }
        givenBackend(list)
    }

    private fun collect() {
        val list = collector.collect()
        PerformanceWriter().write(list, outputFile)
    }

    private fun givenBackend(list: List<SimpleRunTest>) {

        given(reports.getTestsForRunId(id)).willReturn(
            Try.Success(list)
        )

        list.forEach {
            given(reports.getPerformanceTest(it.id)).willReturn(
                Try.Success(
                    PerformanceTest(
                        it.name,
                        it.id,
                        series = mapOf("${it.name}_fps" to listOf(22.0))
                    )
                )
            )
        }
    }
}
