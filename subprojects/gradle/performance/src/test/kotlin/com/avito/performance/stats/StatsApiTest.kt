package com.avito.performance.stats

import com.avito.performance.stats.comparison.ComparedTest
import com.avito.utils.logging.CILogger
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class StatsApiTest {

    private val mockWebServer = MockWebServer()
    private val logger = CILogger.allToStdout
    private lateinit var statsApi: StatsApi

    @BeforeEach
    fun setup() {
        mockWebServer.start()
        val url = mockWebServer.url("/").toString()
        statsApi = StatsApi.Impl(url = url, verbose = true, logger = logger)
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `compare - returns list of results`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(answer)
        )

        val result = statsApi.compare(listOf())

        assertThat(result).isEqualTo(
            listOf(
                ComparedTest.Result(
                    testName = "com.avito.android.test.performance.start.StartApp::collectStartupTime",
                    series = mapOf(
                        "AvitoStartupTime" to ComparedTest.Series(
                            significance = 0.1,
                            currentSampleIs = ComparedTest.State.SAME,
                            statistic = 0.0,
                            pValue = 0.2,
                            meanDiff = 2.0,
                            threshold = 100.0
                        )
                    )
                )
            )
        )
    }


}

private const val answer = """
[
   {
      "testName" : "com.avito.android.test.performance.start.StartApp::collectStartupTime",
      "series" : {
         "AvitoStartupTime" : {
            "pValue" : 0.2,
            "statistic" : 0.0,
            "significance" : 1e-01,
            "currentSampleIs" : "same",
            "meanDiff" : 2.0,
            "threshold" : 100.0
         }
      }
   }
]
"""
