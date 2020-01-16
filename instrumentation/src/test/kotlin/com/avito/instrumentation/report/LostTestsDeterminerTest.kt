package com.avito.instrumentation.report

import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.isInstanceOf
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test

internal class LostTestsDeterminerTest {

    @Test
    fun `determine - AllTestsReported - when all tests found in report`() {
        val result = LostTestsDeterminerImplementation()
            .determine(
                runResult = Try.Success(
                    listOf(
                        SimpleRunTest.createStubInstance(name = "com.Test.test1", deviceName = "api22"),
                        SimpleRunTest.createStubInstance(name = "com.Test.test2", deviceName = "api22"),
                        SimpleRunTest.createStubInstance(name = "com.Test.test3", deviceName = "api22")
                    )
                ),
                initialTestsToRun = listOf(
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test1", deviceName = "api22"),
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test2", deviceName = "api22"),
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test3", deviceName = "api22")
                )
            )

        assertThat(result).isInstanceOf<LostTestsDeterminer.Result.AllTestsReported>()
    }

    @Test
    fun `determine - ThereWereMissedTests - when not all tests found in report`() {
        val result = LostTestsDeterminerImplementation()
            .determine(
                runResult = Try.Success(
                    listOf(
                        SimpleRunTest.createStubInstance(name = "com.Test.test1", deviceName = "api22"),
                        SimpleRunTest.createStubInstance(name = "com.Test.test3", deviceName = "api22")
                    )
                ),
                initialTestsToRun = listOf(
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test1", deviceName = "api22"),
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test2", deviceName = "api22"),
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test3", deviceName = "api22")
                )
            )

        assertThat(result).isInstanceOf<LostTestsDeterminer.Result.ThereWereLostTests>()
    }

    @Test
    fun `determine - FailedToGetMissedTests - when failed to get tests from report`() {
        val result = LostTestsDeterminerImplementation()
            .determine(
                runResult = Try.Failure(RuntimeException()),
                initialTestsToRun = listOf(
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test1", deviceName = "api22"),
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test2", deviceName = "api22"),
                    TestStaticDataPackage.createStubInstance(name = "com.Test.test3", deviceName = "api22")
                )
            )

        assertThat(result).isInstanceOf<LostTestsDeterminer.Result.FailedToGetLostTests>()
    }
}
