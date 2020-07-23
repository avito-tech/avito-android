package com.avito.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.Entry
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Step
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.createStubInstance
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockReportsExtension::class)
internal class StepsTest {

    private fun testRequest(reports: MockReportApi) = reports.addTest(
        reportCoordinates = ReportCoordinates.createStubInstance(),
        buildId = "1234",
        test = AndroidTest.Completed.createStubInstance(
            testRuntimeData = TestRuntimeDataPackage.createStubInstance(
                steps = listOf(
                    Step(
                        timestamp = 1570044898,
                        number = 1,
                        title = "Hello",
                        entryList = listOf(
                            Entry.Comment(title = "Hello from comment", timeInMs = 1570044922)
                        )
                    )
                )
            )
        )
    )

    @Test
    fun `step contains title`(reports: MockReportApi) {
        testRequest(reports)
            .singleRequestCaptured()
            .bodyMatches(
                hasJsonPath(
                    "$.params.report.test_case_step_list[0].title",
                    Matchers.equalTo("Hello")
                )
            )
    }

    @Test
    fun `step contains number`(reports: MockReportApi) {
        testRequest(reports)
            .singleRequestCaptured()
            .bodyMatches(
                hasJsonPath(
                    "$.params.report.test_case_step_list[0].number",
                    Matchers.equalTo(1)
                )
            )
    }

    @Test
    fun `step contains timestamp`(reports: MockReportApi) {
        testRequest(reports)
            .singleRequestCaptured()
            .bodyMatches(
                hasJsonPath(
                    "$.params.report.test_case_step_list[0].timestamp",
                    Matchers.equalTo(1570044898)
                )
            )
    }

    @Test
    fun `step contains entry with type`(reports: MockReportApi) {
        testRequest(reports)
            .singleRequestCaptured()
            .bodyMatches(
                hasJsonPath(
                    "$.params.report.test_case_step_list[0].entry_list[0].type",
                    Matchers.equalTo("comment")
                )
            )
    }

    @Test
    fun `step contains entry with timestamp`(reports: MockReportApi) {
        testRequest(reports)
            .singleRequestCaptured()
            .bodyMatches(
                hasJsonPath(
                    "$.params.report.test_case_step_list[0].entry_list[0].timestamp",
                    Matchers.equalTo(1570044922)
                )
            )
    }

    @Test
    fun `step contains comment entry with title`(reports: MockReportApi) {
        testRequest(reports)
            .singleRequestCaptured()
            .bodyMatches(
                hasJsonPath(
                    "$.params.report.test_case_step_list[0].entry_list[0].title",
                    Matchers.equalTo("Hello from comment")
                )
            )
    }
}
