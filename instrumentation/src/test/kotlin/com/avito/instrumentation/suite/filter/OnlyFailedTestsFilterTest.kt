package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.instrumentation.suite.dex.createStubInstance
import com.avito.report.model.DeviceName
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Status
import com.avito.report.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class OnlyFailedTestsFilterTest {

    private val deviceName = DeviceName("api22")
    private val api = 22

    @Test
    fun `filter - run - test not found in previous result`() {
        val previousRunResults = emptyList<SimpleRunTest>()

        val runNeeded = OnlyFailedTestsFilter(testRunResults = previousRunResults)
            .runNeeded(
                test = TestInApk.createStubInstance(),
                deviceName = deviceName,
                api = api
            )

        assertThat(runNeeded).isInstanceOf(TestRunFilter.Verdict.Run::class.java)
    }

    @Test
    fun `filter - skipped - manual test`() {
        val previousRunResults = listOf(
            SimpleRunTest.createStubInstance(
                name = "com.Test.test1",
                deviceName = deviceName.name,
                status = Status.Manual
            )
        )

        val runNeeded = OnlyFailedTestsFilter(testRunResults = previousRunResults)
            .runNeeded(
                test = TestInApk.createStubInstance(className = "com.Test", methodName = "test1"),
                deviceName = deviceName,
                api = api
            )

        assertThat(runNeeded).isInstanceOf(TestRunFilter.Verdict.Skip::class.java)
    }

    @Test
    fun `filter - skipped - skipped test`() {
        val previousRunResults = listOf(
            SimpleRunTest.createStubInstance(
                name = "com.Test.test1",
                deviceName = deviceName.name,
                status = Status.Skipped("")
            )
        )

        val runNeeded = OnlyFailedTestsFilter(testRunResults = previousRunResults)
            .runNeeded(
                test = TestInApk.createStubInstance(className = "com.Test", methodName = "test1"),
                deviceName = deviceName,
                api = api
            )

        assertThat(runNeeded).isInstanceOf(TestRunFilter.Verdict.Skip::class.java)
    }

    @Test
    fun `filter - skipped - test found and succeed`() {
        val previousRunResults = listOf(
            SimpleRunTest.createStubInstance(
                name = "com.Test.test1",
                deviceName = deviceName.name,
                status = Status.Success
            )
        )

        val runNeeded = OnlyFailedTestsFilter(testRunResults = previousRunResults)
            .runNeeded(
                test = TestInApk.createStubInstance(
                    className = "com.Test",
                    methodName = "test1"
                ),
                deviceName = deviceName,
                api = api
            )

        assertThat(runNeeded).isInstanceOf(TestRunFilter.Verdict.Skip::class.java)
    }

    @Test
    fun `filter - run - test found and failed`() {
        val previousRunResults = listOf(
            SimpleRunTest.createStubInstance(
                name = "com.Test.test1",
                deviceName = deviceName.name,
                status = Status.Failure("", "")
            )
        )

        val runNeeded = OnlyFailedTestsFilter(testRunResults = previousRunResults)
            .runNeeded(
                test = TestInApk.createStubInstance(
                    className = "com.Test",
                    methodName = "test1"
                ),
                deviceName = deviceName,
                api = api
            )

        assertThat(runNeeded).isInstanceOf(TestRunFilter.Verdict.Run::class.java)
    }

    @Test
    fun `filter - run - test found and lost`() {
        val previousRunResults = listOf(
            SimpleRunTest.createStubInstance(
                name = "com.Test.test1",
                deviceName = deviceName.name,
                status = Status.Lost
            )
        )

        val runNeeded = OnlyFailedTestsFilter(testRunResults = previousRunResults)
            .runNeeded(
                test = TestInApk.createStubInstance(
                    className = "com.Test",
                    methodName = "test1"
                ),
                deviceName = deviceName,
                api = api
            )

        assertThat(runNeeded).isInstanceOf(TestRunFilter.Verdict.Run::class.java)
    }
}
