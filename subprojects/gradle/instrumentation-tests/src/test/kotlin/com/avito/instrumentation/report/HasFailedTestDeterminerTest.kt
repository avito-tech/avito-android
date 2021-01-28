package com.avito.instrumentation.report

import com.avito.instrumentation.internal.report.HasFailedTestDeterminer
import com.avito.report.model.Flakiness
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Status
import com.avito.report.model.createStubInstance
import com.avito.truth.assertThat
import com.avito.truth.isInstanceOf
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

        assertThat(result).isInstanceOf<HasFailedTestDeterminer.Result.NoFailed>()
    }

    @Test
    fun `determine - results OK - empty test results`() {
        val result = createImpl()
            .determine(
                runResult = Try.Success(
                    listOf()
                )
            )

        assertThat(result).isInstanceOf<HasFailedTestDeterminer.Result.NoFailed>()
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

        assertThat(result).isInstanceOf<HasFailedTestDeterminer.Result.NoFailed>()
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

        assertThat(result).isInstanceOf<HasFailedTestDeterminer.Result.Failed>()
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

        assertThat<HasFailedTestDeterminer.Result.Failed>(result) {
            assertThat(notSuppressedCount).isEqualTo(0)
            assertThat(suppression).isInstanceOf<HasFailedTestDeterminer.Result.Failed.Suppression.SuppressedFlaky>()
            assertThat(suppression.tests).hasSize(1)
            assertThat(suppression.tests[0].name).isEqualTo("com.Test.test2")
        }
    }

    @Test
    fun `determine - results failed - suppress flaky is false`() {
        val result = createImpl(suppressFlaky = false)
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

        assertThat<HasFailedTestDeterminer.Result.Failed>(result) {
            assertThat(notSuppressedCount).isEqualTo(1)
            assertThat(notSuppressed[0].name).isEqualTo("com.Test.test2")
            assertThat(suppression).isInstanceOf<HasFailedTestDeterminer.Result.Failed.Suppression.NoSuppressed>()
        }
    }

    @Test
    fun `determine - results failed - suppress flaky is true but there is failed stable test`() {
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
                            deviceName = "api22",
                            status = Status.Failure("", "")
                        )
                    )
                )
            )

        assertThat<HasFailedTestDeterminer.Result.Failed>(result) {
            assertThat(notSuppressedCount).isEqualTo(1)
            assertThat(notSuppressed[0].name).isEqualTo("com.Test.test3")
            assertThat(suppression).isInstanceOf<HasFailedTestDeterminer.Result.Failed.Suppression.SuppressedFlaky>()
            assertThat(suppression.tests).hasSize(1)
            assertThat(suppression.tests[0].name).isEqualTo("com.Test.test2")
        }
    }

    private fun createImpl(
        suppressFailure: Boolean = false,
        suppressFlaky: Boolean = false
    ) = HasFailedTestDeterminer.Impl(
        suppressFailure = suppressFailure,
        suppressFlaky = suppressFlaky
    )
}
