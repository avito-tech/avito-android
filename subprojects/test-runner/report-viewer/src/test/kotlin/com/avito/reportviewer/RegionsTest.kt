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
internal class RegionsTest {

    @Test
    fun `regions provided sending test prepared_data contains regions`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(
                    regions = listOf("region1", "region2", "region3")
                )
            )
        )
            .singleRequestCaptured()
            .bodyMatches(
                hasJsonPath(
                    "$.params.prepared_data.regions",
                    Matchers.equalTo(listOf("region1", "region2", "region3"))
                )
            )
    }

    @Test
    fun `regions missing sending test prepared_data omits regions`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(regions = emptyList())
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasNoJsonPath("$.params.prepared_data.regions"))
    }
}
