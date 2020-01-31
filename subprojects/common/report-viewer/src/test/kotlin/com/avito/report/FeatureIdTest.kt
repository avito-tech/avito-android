package com.avito.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockReportsExtension::class)
internal class FeatureIdTest {

    @Test
    fun `feature_id sent`(reports: MockReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(featureIds = listOf(256, 512))
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.prepared_data.feature_id", Matchers.equalTo(listOf(256, 512))))
    }
}
