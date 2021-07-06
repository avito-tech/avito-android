package com.avito.reportviewer

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.createStubInstance
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.createStubInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExtendWith(StubReportsExtension::class)
internal class TestDurationTest {

    @Test
    fun `startTime set`(reports: StubReportApi) {
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
    fun `endTime set`(reports: StubReportApi) {
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
