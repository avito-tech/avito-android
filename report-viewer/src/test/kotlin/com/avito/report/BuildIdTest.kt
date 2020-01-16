package com.avito.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.createStubInstance
import com.jayway.jsonpath.matchers.JsonPathMatchers
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockReportsExtension::class)
class BuildIdTest {

    @Test
    fun `buildId added to list via mongo method hack`(reports: MockReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1002134",
            test = AndroidTest.Completed.createStubInstance()
        )
            .singleRequestCaptured()
            .bodyMatches(
                JsonPathMatchers.hasJsonPath(
                    "$.params.report_data.build_id_set.\$fillSet[0]",
                    Matchers.equalTo("1002134")
                )
            )
    }

    @Test
    fun `prepared data tc_build contains buildId`(reports: MockReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1002134",
            test = AndroidTest.Completed.createStubInstance()
        )
            .singleRequestCaptured()
            .bodyMatches(
                JsonPathMatchers.hasJsonPath(
                    "$.params.prepared_data.tc_build",
                    Matchers.equalTo("1002134")
                )
            )
    }

    @Test
    fun `prepared data doesn't contain tc_build for local build`(reports: MockReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = null,
            test = AndroidTest.Completed.createStubInstance()
        )
            .singleRequestCaptured()
            .bodyMatches(JsonPathMatchers.hasNoJsonPath("$.params.prepared_data.tc_build"))
    }

    @Test
    fun `report_data doesn't contain for local build`(reports: MockReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = null,
            test = AndroidTest.Completed.createStubInstance()
        )
            .singleRequestCaptured()
            .bodyMatches(JsonPathMatchers.hasNoJsonPath("$.params.report_data.build_id_set"))
    }
}
