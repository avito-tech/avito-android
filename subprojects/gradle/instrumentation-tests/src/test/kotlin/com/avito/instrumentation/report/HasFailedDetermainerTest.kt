package com.avito.instrumentation.report

import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Status
import com.avito.report.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test

internal class HasFailedDetermainerTest {

    @Test
    fun `determine - results OK - all tests success or skipped`() {
        val result = HasFailedTestDeterminer.Impl(suppressFailure = false, suppressGroups = emptyList())
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
                )
            )

        assertThat(result).isInstanceOf(HasFailedTestDeterminer.Result.NoFailed::class.java)
    }

    @Test
    fun `determine - results OK - empty test results`() {
        val result = HasFailedTestDeterminer.Impl(suppressFailure = false, suppressGroups = emptyList())
            .determine(
                runResult = Try.Success(
                    listOf()
                )
            )

        assertThat(result).isInstanceOf(HasFailedTestDeterminer.Result.NoFailed::class.java)
    }

    @Test
    fun `determine - results OK - all tests reported`() {
        val result = HasFailedTestDeterminer.Impl(suppressFailure = false, suppressGroups = emptyList())
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
                )
            )

        assertThat(result).isInstanceOf(HasFailedTestDeterminer.Result.NoFailed::class.java)
    }

    @Test
    fun `determine - results failure - one test failed`() {
        val result = HasFailedTestDeterminer.Impl(suppressFailure = false, suppressGroups = emptyList())
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
                )
            )

        assertThat(result).isInstanceOf(HasFailedTestDeterminer.Result.Failed::class.java)
    }
}
