package com.avito.test.summary.compose

import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import org.funktionale.tries.Try

internal interface SlackSummaryComposer {

    fun composeMessage(
        testData: CrossDeviceSuite,
        team: Team,
        mentionOnFailures: Boolean,
        reportCoordinates: ReportCoordinates,
        reportId: String,
        buildUrl: String
    ): Try<String>
}
