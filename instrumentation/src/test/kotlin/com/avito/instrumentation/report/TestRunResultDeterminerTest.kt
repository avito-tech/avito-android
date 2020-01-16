package com.avito.instrumentation.report

import com.avito.instrumentation.TestRunResult
import com.avito.report.model.AndroidTest
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Status
import com.avito.report.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test

internal class TestRunResultDeterminerTest {

    @Test
    fun `determine - results OK - all tests success or skipped`() {
        val result = TestRunResultDeterminerImplementation(suppressFailure = false, suppressGroups = emptyList())
            .determine(
                runResult = Try.Success(
                    listOf(
                        SimpleRunTest.createStubInstance(
                            name = "com.Test.test1",
                            deviceName = "api22"
                        ),
                        SimpleRunTest.createStubInstance(
                            name = "com.Test.test2",
                            deviceName = "api22",
                            status = Status.Skipped("because")
                        ),
                        SimpleRunTest.createStubInstance(
                            name = "com.Test.test3",
                            deviceName = "api22"
                        )
                    )
                ),
                lostTestsResult = LostTestsDeterminer.Result.AllTestsReported
            )

        assertThat(result).isInstanceOf(TestRunResult.OK::class.java)
    }

    @Test
    fun `determine - results OK - empty test results`() {
        val result = TestRunResultDeterminerImplementation(suppressFailure = false, suppressGroups = emptyList())
            .determine(
                runResult = Try.Success(
                    listOf()
                ),
                lostTestsResult = LostTestsDeterminer.Result.AllTestsReported
            )

        assertThat(result).isInstanceOf(TestRunResult.OK::class.java)
    }

    @Test
    fun `determine - results OK - all tests reported`() {
        val result = TestRunResultDeterminerImplementation(suppressFailure = false, suppressGroups = emptyList())
            .determine(
                runResult = Try.Success(
                    listOf(
                        SimpleRunTest.createStubInstance(
                            name = "com.Test.test1",
                            deviceName = "api22"
                        ),
                        SimpleRunTest.createStubInstance(
                            name = "com.Test.test2",
                            deviceName = "api22"
                        ),
                        SimpleRunTest.createStubInstance(
                            name = "com.Test.test3",
                            deviceName = "api22"
                        )
                    )
                ),
                lostTestsResult = LostTestsDeterminer.Result.AllTestsReported
            )

        assertThat(result).isInstanceOf(TestRunResult.OK::class.java)
    }

    @Test
    fun `determine - results failure - one test failed`() {
        val result = TestRunResultDeterminerImplementation(suppressFailure = false, suppressGroups = emptyList())
            .determine(
                runResult = Try.Success(
                    listOf(
                        SimpleRunTest.createStubInstance(
                            name = "com.Test.test1",
                            deviceName = "api22"
                        ),
                        SimpleRunTest.createStubInstance(
                            name = "com.Test.test2",
                            deviceName = "api22",
                            status = Status.Failure("", "")
                        ),
                        SimpleRunTest.createStubInstance(
                            name = "com.Test.test3",
                            deviceName = "api22"
                        )
                    )
                ),
                lostTestsResult = LostTestsDeterminer.Result.AllTestsReported
            )

        assertThat(result).isInstanceOf(TestRunResult.Failure::class.java)
    }

    @Test
    fun `determine - results OK - even if missed tests found`() {
        val result = TestRunResultDeterminerImplementation(suppressFailure = false, suppressGroups = emptyList())
            .determine(
                runResult = Try.Success(
                    listOf(
                        SimpleRunTest.createStubInstance(
                            name = "com.Test.test1",
                            deviceName = "api22"
                        )
                    )
                ),
                lostTestsResult = LostTestsDeterminer.Result.ThereWereLostTests(
                    lostTests = listOf(
                        AndroidTest.Lost.createStubInstance(
                            name = "com.Test.test1",
                            deviceName = "device"
                        )
                    )
                )
            )

        assertThat(result).isInstanceOf(TestRunResult.OK::class.java)
    }

    @Test
    fun `determine - results failure - skipped tests determine result failed`() {
        val result = TestRunResultDeterminerImplementation(suppressFailure = false, suppressGroups = emptyList())
            .determine(
                runResult = Try.Success(
                    listOf(
                        SimpleRunTest.createStubInstance(
                            name = "com.Test.test1",
                            deviceName = "api22"
                        )
                    )
                ),
                lostTestsResult = LostTestsDeterminer.Result.FailedToGetLostTests(
                    exception = RuntimeException()
                )
            )

        assertThat(result).isInstanceOf(TestRunResult.Failure::class.java)
    }
}
