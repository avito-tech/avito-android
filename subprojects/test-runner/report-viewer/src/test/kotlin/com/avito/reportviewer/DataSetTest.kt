package com.avito.reportviewer

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(StubReportsExtension::class)
internal class DataSetTest {

    @Test
    fun `data_set doesnt sent for empty map`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testRuntimeData = TestRuntimeDataPackage.createStubInstance(dataSetData = emptyMap())
            )
        )
            .singleRequestCaptured()
            .bodyDoesntContain("\"data_set\"")
    }

    @Test
    fun `data_set_number sent`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(dataSetNumber = 1)
            )
        )
            .singleRequestCaptured()
            .bodyContains("\"data_set_number\":\"1\"")
    }

    @Test
    fun `data_set object sent`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(dataSetNumber = 1),
                testRuntimeData = TestRuntimeDataPackage.createStubInstance(dataSetData = mapOf("vas" to "premium"))
            )
        )
            .singleRequestCaptured()
            .bodyContains("\"data_set\":{\"vas\":\"premium\"}}")
    }

    @Test
    fun `data_set without data_set_number throws an exception`(reports: StubReportApi) {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            reports.addTest(
                reportCoordinates = ReportCoordinates.createStubInstance(),
                buildId = "1234",
                test = AndroidTest.Completed.createStubInstance(
                    testStaticData = TestStaticDataPackage.createStubInstance(dataSetNumber = null),
                    testRuntimeData = TestRuntimeDataPackage.createStubInstance(dataSetData = mapOf("vas" to "premium"))
                )
            )
        }

        assertThat(exception).hasMessageThat().isEqualTo("DataSet data without DataSetNumber doesn't make sense!")
    }
}
