package com.avito.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.createStubInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExtendWith(MockReportsExtension::class)
class TestDurationTest {

    @Test
    fun `startTime set`(reports: MockReportApi) {
        val now = Date().time
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testRuntimeData = TestRuntimeDataPackage.createStubInstance(
                    startTime = now
                )
            )
        )
            .singleRequestCaptured()
            .bodyContains("\"start_time\":$now")
    }

    @Test
    fun `endTime set`(reports: MockReportApi) {
        val now = Date().time
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testRuntimeData = TestRuntimeDataPackage.createStubInstance(
                    endTime = now
                )
            )
        )
            .singleRequestCaptured()
            .bodyContains("\"end_time\":$now")
    }
}
