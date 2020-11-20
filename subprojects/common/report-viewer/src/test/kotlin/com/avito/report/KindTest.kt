package com.avito.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.Kind
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockReportsExtension::class)
internal class KindTest {

    @Test
    fun `kind e2e sent`(reports: MockReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(kind = Kind.E2E)
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.kind", Matchers.equalTo("e2e")))
    }

    @Test
    fun `kind component sent`(reports: MockReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(kind = Kind.UI_COMPONENT)
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.kind", Matchers.equalTo("ui-component")))
    }

    @Test
    fun `kind manual sent`(reports: MockReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(kind = Kind.MANUAL)
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.kind", Matchers.equalTo("manual")))
    }
}
