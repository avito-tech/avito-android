package com.avito.reportviewer

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.createStubInstance
import com.avito.test.model.TestName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(StubReportsExtension::class)
internal class GroupingKeyTest {

    @Test
    fun `grouping_key sent for dataSet without testCaseId`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    testCaseId = null,
                    dataSetNumber = 1,
                    name = TestName("com.avito.Test", "test")
                )
            )
        )
            .singleRequestCaptured()
            .bodyContains("\"grouping_key\":\"com.avito.Test\"")
    }

    @Test
    fun `grouping_key doesnt sent for dataSet with testCaseId`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    testCaseId = 12345,
                    dataSetNumber = 1,
                    name = TestName("com.avito.Test", "test")
                )
            )
        )
            .singleRequestCaptured()
            .bodyDoesntContain("grouping_key")
    }

    @Test
    fun `grouping_key doesnt sent for testCase without dataset`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    testCaseId = 12345,
                    dataSetNumber = null,
                    name = TestName("com.avito.Test", "test")
                )
            )
        )
            .singleRequestCaptured()
            .bodyDoesntContain("grouping_key")
    }
}
