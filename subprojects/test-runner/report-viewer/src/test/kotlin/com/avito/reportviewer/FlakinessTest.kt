package com.avito.reportviewer

import com.avito.report.model.AndroidTest
import com.avito.report.model.Flakiness
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.createStubInstance
import com.jayway.jsonpath.matchers.JsonPathMatchers
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(StubReportsExtension::class)
internal class FlakinessTest {

    @Test
    fun `prepared data contains flakiness - test is stable`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1002134",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    flakiness = Flakiness.Stable
                )
            )
        )
            .singleRequestCaptured()
            .bodyMatches(
                JsonPathMatchers.hasJsonPath(
                    "$.params.prepared_data.is_flaky",
                    Matchers.equalTo(false)
                )
            )
            .bodyMatches(
                JsonPathMatchers.hasNoJsonPath(
                    "$.params.prepared_data.flaky_reason"
                )
            )
    }

    @Test
    fun `prepared data contains flakiness - test is flaky`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1002134",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    flakiness = Flakiness.Flaky("Just because")
                )
            )
        )
            .singleRequestCaptured()
            .bodyMatches(
                JsonPathMatchers.hasJsonPath(
                    "$.params.prepared_data.is_flaky",
                    Matchers.equalTo(true)
                )
            )
            .bodyMatches(
                JsonPathMatchers.hasJsonPath(
                    "$.params.prepared_data.flaky_reason",
                    Matchers.equalTo("Just because")
                )
            )
    }
}
