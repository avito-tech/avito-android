package com.avito.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(StubReportsExtension::class)
class TestCaseIdTest {

    @Test
    fun `testCaseId doesnt sent if null`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    testCaseId = null
                )
            )
        )
            .singleRequestCaptured()
            .bodyDoesntContain("\"test_case_id\"")
    }

    @Test
    fun `grouping_key sent for dataSet without testCaseId`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    testCaseId = 21345
                )
            )
        )
            .singleRequestCaptured()
            .bodyContains("\"test_case_id\":\"21345\"")
    }
}
