package com.avito.instrumentation.report

import com.avito.report.model.Flakiness
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Status
import com.avito.report.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test

internal class HasFailedTestDeterminerTest {

    @Test
    fun `determine - results OK - all tests success or skipped`() {
        val result = createImpl()
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
        val result = createImpl()
            .determine(
                runResult = Try.Success(
                    listOf()
                )
            )

        assertThat(result).isInstanceOf(HasFailedTestDeterminer.Result.NoFailed::class.java)
    }

    @Test
    fun `determine - results OK - all tests reported`() {
        val result = createImpl()
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
        val result = createImpl()
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

    @Test
    fun `determine - results failed - suppress flaky is true`() {
        val result = createImpl(suppressFlaky = true)
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
                            status = Status.Failure("", ""),
                            flakiness = Flakiness.Flaky("oops")
                        ),
                        SimpleRunTest.createStubInstance(
                            name = "com.Test.test3",
                            deviceName = "api22"
                        )
                    )
                )
            )

        assertThat(result).isInstanceOf(HasFailedTestDeterminer.Result.FailedWithSuppressed::class.java)
        val failed = result as HasFailedTestDeterminer.Result.FailedWithSuppressed
        assertThat(failed.suppressed.tests).hasSize(1)
        assertThat(failed.suppressed.tests[0].name).isEqualTo("com.Test.test2")
        assertThat(failed.suppressed.reason).isInstanceOf(
            HasFailedTestDeterminer.Result.FailedWithSuppressed.Suppressed.Reason.SuppressedByFlakiness::class.java
        )
    }

    private fun createImpl(
        suppressFailure: Boolean = false,
        suppressFlaky: Boolean = false
    ) = HasFailedTestDeterminer.Impl(
        suppressFailure = suppressFailure,
        suppressFlaky = suppressFlaky
    )
}
