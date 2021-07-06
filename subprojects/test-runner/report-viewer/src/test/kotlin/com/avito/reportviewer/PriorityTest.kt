package com.avito.reportviewer

import com.avito.android.test.annotations.TestCasePriority
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.createStubInstance
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(StubReportsExtension::class)
internal class PriorityTest {

    @Test
    fun `priority major sent in prepared_data`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(priority = TestCasePriority.MAJOR)
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.prepared_data.priority_id", Matchers.equalTo(3)))
    }

    @Test
    fun `priority minor sent in prepared_data`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(priority = TestCasePriority.MINOR)
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.prepared_data.priority_id", Matchers.equalTo(1)))
    }
}
