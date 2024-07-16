package com.avito.test.summary.compose

import com.avito.android.Result
import com.avito.report.model.Team
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.summary.model.CrossDeviceSuite

internal interface TestSummaryComposer {

    fun composeMessage(
        testData: CrossDeviceSuite,
        team: Team,
        reportCoordinates: ReportCoordinates,
        reportId: String,
        buildUrl: String
    ): Result<String>
}
