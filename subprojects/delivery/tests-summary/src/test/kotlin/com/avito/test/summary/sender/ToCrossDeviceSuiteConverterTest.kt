package com.avito.test.summary.sender

import com.avito.report.model.TestStatus
import com.avito.reportviewer.model.SimpleRunTest
import com.avito.reportviewer.model.createStubInstance
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

class ToCrossDeviceSuiteConverterTest {

    @Test
    fun `all failed or skipped - means failed on all devices`() {
        val simpleRunTestList = listOf(
            SimpleRunTest.createStubInstance(status = TestStatus.Skipped("")),
            SimpleRunTest.createStubInstance(status = TestStatus.Failure("")),
        )

        val suite = ToCrossDeviceSuiteConverter.convert(simpleRunTestList)

        Truth.assertThat(suite.failedOnAllDevicesCount).isEqualTo(1)
    }

    @Test
    fun `all failed - means failed on all devices`() {
        val simpleRunTestList = listOf(
            SimpleRunTest.createStubInstance(status = TestStatus.Failure("")),
            SimpleRunTest.createStubInstance(status = TestStatus.Failure("")),
        )

        val suite = ToCrossDeviceSuiteConverter.convert(simpleRunTestList)

        Truth.assertThat(suite.failedOnAllDevicesCount).isEqualTo(1)
    }
}
