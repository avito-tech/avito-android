package com.avito.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.TestName
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(StubReportsExtension::class)
internal class TestNameTest {

    @Test
    fun `report with dataSet - name contains dataset number - with testCaseId`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    testCaseId = 123,
                    name = TestName("com.avito.Test", "myTest"),
                    dataSetNumber = 2
                )
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.report.test_name", equalTo("myTest#2")))
    }

    @Test
    fun `report with dataSet - name contains dataset number - no testCaseId`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    testCaseId = null,
                    name = TestName("com.avito.Test", "someDataSet"),
                    dataSetNumber = 2
                )
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.report.test_name", equalTo("someDataSet#2")))
    }

    @Test
    fun `report without dataSet - have testName without # - with testCaseId`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    testCaseId = 123,
                    name = TestName("com.avito.Test", "someTest"),
                    dataSetNumber = null
                )
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.report.test_name", equalTo("someTest")))
    }

    @Test
    fun `report without dataSet - have testName without # - no testCaseId`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    testCaseId = null,
                    name = TestName("com.avito.Test", "someTest"),
                    dataSetNumber = null
                )
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.report.test_name", equalTo("someTest")))
    }
}
