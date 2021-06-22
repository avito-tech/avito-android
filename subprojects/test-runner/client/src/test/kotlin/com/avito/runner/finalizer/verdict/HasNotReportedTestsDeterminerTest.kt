package com.avito.runner.finalizer.verdict

import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.test.model.DeviceName
import com.avito.test.model.TestName
import com.avito.time.StubTimeProvider
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class HasNotReportedTestsDeterminerTest {

    private val timeProvider = StubTimeProvider()

    @Test
    fun `determine - AllTestsReported - when all tests found in report`() {
        val result = LegacyNotReportedTestsDeterminer(timeProvider)
            .determine(
                runResult = listOf(
                    SimpleRunTest.createStubInstance(name = TestName("com.Test", "test1"), deviceName = "api22"),
                    SimpleRunTest.createStubInstance(name = TestName("com.Test", "test2"), deviceName = "api22"),
                    SimpleRunTest.createStubInstance(name = TestName("com.Test", "test3"), deviceName = "api22")
                ),
                allTests = listOf(
                    TestStaticDataPackage.createStubInstance(
                        name = TestName("com.Test", "test1"),
                        deviceName = DeviceName("api22")
                    ),
                    TestStaticDataPackage.createStubInstance(
                        name = TestName("com.Test", "test2"),
                        deviceName = DeviceName("api22")
                    ),
                    TestStaticDataPackage.createStubInstance(
                        name = TestName("com.Test", "test3"),
                        deviceName = DeviceName("api22")
                    )
                )
            )

        assertThat(result).isInstanceOf<HasNotReportedTestsDeterminer.Result.AllTestsReported>()
    }

    @Test
    fun `determine - ThereWereMissedTests - when not all tests found in report`() {
        val result = LegacyNotReportedTestsDeterminer(timeProvider)
            .determine(
                runResult = listOf(
                    SimpleRunTest.createStubInstance(name = TestName("com.Test", "test1"), deviceName = "api22"),
                    SimpleRunTest.createStubInstance(name = TestName("com.Test", "test3"), deviceName = "api22")
                ),
                allTests = listOf(
                    TestStaticDataPackage.createStubInstance(
                        name = TestName("com.Test", "test1"),
                        deviceName = DeviceName("api22")
                    ),
                    TestStaticDataPackage.createStubInstance(
                        name = TestName("com.Test", "test2"),
                        deviceName = DeviceName("api22")
                    ),
                    TestStaticDataPackage.createStubInstance(
                        name = TestName("com.Test", "test3"),
                        deviceName = DeviceName("api22")
                    )
                )
            )

        assertThat(result).isInstanceOf<HasNotReportedTestsDeterminer.Result.HasNotReportedTests>()
    }
}
