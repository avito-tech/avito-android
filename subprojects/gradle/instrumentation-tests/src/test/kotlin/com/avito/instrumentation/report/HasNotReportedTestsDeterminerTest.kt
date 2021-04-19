package com.avito.instrumentation.report

import com.avito.instrumentation.internal.report.HasNotReportedTestsDeterminer
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class HasNotReportedTestsDeterminerTest {

    @Test
    fun `determine - AllTestsReported - when all tests found in report`() {
        val result = HasNotReportedTestsDeterminer.Impl()
            .determine(
                runResult = listOf(
                    SimpleRunTest.createStubInstance(name = "com.Test.test1", deviceName = "api22"),
                    SimpleRunTest.createStubInstance(name = "com.Test.test2", deviceName = "api22"),
                    SimpleRunTest.createStubInstance(name = "com.Test.test3", deviceName = "api22")
                ),
                allTests = listOf(
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test1", deviceName = "api22"),
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test2", deviceName = "api22"),
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test3", deviceName = "api22")
                )
            )

        assertThat(result).isInstanceOf<HasNotReportedTestsDeterminer.Result.AllTestsReported>()
    }

    @Test
    fun `determine - ThereWereMissedTests - when not all tests found in report`() {
        val result = HasNotReportedTestsDeterminer.Impl()
            .determine(
                runResult = listOf(
                    SimpleRunTest.createStubInstance(name = "com.Test.test1", deviceName = "api22"),
                    SimpleRunTest.createStubInstance(name = "com.Test.test3", deviceName = "api22")
                ),
                allTests = listOf(
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test1", deviceName = "api22"),
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test2", deviceName = "api22"),
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test3", deviceName = "api22")
                )
            )

        assertThat(result).isInstanceOf<HasNotReportedTestsDeterminer.Result.HasNotReportedTests>()
    }

    @Test
    fun `determine - FailedToGetMissedTests - when failed to get tests from report`() {
        val result = HasNotReportedTestsDeterminer.Impl()
            .determine(
                runResult = emptyList(),
                allTests = listOf(
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test1", deviceName = "api22"),
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test2", deviceName = "api22"),
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test3", deviceName = "api22")
                )
            )

        assertThat(result).isInstanceOf<HasNotReportedTestsDeterminer.Result.DetermineError>()
    }
}
