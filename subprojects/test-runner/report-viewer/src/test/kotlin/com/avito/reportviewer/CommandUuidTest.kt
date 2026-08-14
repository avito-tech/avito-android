package com.avito.reportviewer

import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.createStubInstance
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(StubReportsExtension::class)
internal class CommandUuidTest {

    @Test
    fun `command uuid provided sending test prepared_data contains command uuid`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    commandUuid = "b5a5ff6d-73ef-400e-abda-6a89b19e4729"
                )
            )
        )
            .singleRequestCaptured()
            .bodyMatches(
                hasJsonPath(
                    "$.params.prepared_data.command_uuid",
                    Matchers.equalTo("b5a5ff6d-73ef-400e-abda-6a89b19e4729")
                )
            )
    }

    @Test
    fun `command uuid provided sending skipped test prepared_data contains command uuid`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Skipped.createStubInstance(
                commandUuid = "b5a5ff6d-73ef-400e-abda-6a89b19e4729"
            )
        )
            .singleRequestCaptured()
            .bodyMatches(
                hasJsonPath(
                    "$.params.prepared_data.command_uuid",
                    Matchers.equalTo("b5a5ff6d-73ef-400e-abda-6a89b19e4729")
                )
            )
    }

    @Test
    fun `blank command uuid sending test prepared_data omits command uuid`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(commandUuid = "   ")
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasNoJsonPath("$.params.prepared_data.command_uuid"))
    }

    @Test
    fun `command uuid missing sending test prepared_data omits command uuid`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(commandUuid = null)
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasNoJsonPath("$.params.prepared_data.command_uuid"))
    }
}
