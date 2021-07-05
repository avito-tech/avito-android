package com.avito.test.summary.compose

import com.avito.android.Result
import com.avito.reportviewer.model.CrossDeviceSuite
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.reportviewer.model.Team

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
