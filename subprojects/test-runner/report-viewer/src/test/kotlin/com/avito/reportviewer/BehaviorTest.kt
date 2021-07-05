package com.avito.reportviewer

import com.avito.android.test.annotations.TestCaseBehavior
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
internal class BehaviorTest {

    @Test
    fun `behavior positive sent in prepared_data`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(behavior = TestCaseBehavior.POSITIVE)
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.prepared_data.behavior_id", Matchers.equalTo(2)))
    }

    @Test
    fun `behavior negative sent in prepared_data`(reports: StubReportApi) {
        reports.addTest(
            reportCoordinates = ReportCoordinates.createStubInstance(),
            buildId = "1234",
            test = AndroidTest.Completed.createStubInstance(
                testStaticData = TestStaticDataPackage.createStubInstance(behavior = TestCaseBehavior.NEGATIVE)
            )
        )
            .singleRequestCaptured()
            .bodyMatches(hasJsonPath("$.params.prepared_data.behavior_id", Matchers.equalTo(3)))
    }
}
