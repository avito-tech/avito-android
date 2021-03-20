package com.avito.test.summary.compose

import com.avito.android.Result
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team

internal interface SlackSummaryComposer {

    fun composeMessage(
        testData: CrossDeviceSuite,
        team: Team,
        mentionOnFailures: Boolean,
        reportCoordinates: ReportCoordinates,
        reportId: String,
        buildUrl: String
    ): Result<String>
}
