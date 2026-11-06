package com.avito.test.summary.sender

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.summary.compose.TestSummaryComposer
import com.avito.test.summary.compose.TestSummaryComposerImpl
import com.avito.test.summary.model.CrossDeviceSuite
import com.avito.test.summary.model.TestSummaryDestination
import com.avito.test.summary.time.TimeMessenger

/**
 * Duplicates team summaries to Time for teams with `timeChannelId` in the destinations file.
 */
internal class TimeTestSummarySender(
    reportViewerUrl: String,
    private val buildUrl: String,
    private val reportCoordinates: ReportCoordinates,
    private val destinations: List<TestSummaryDestination>,
    private val timeMessenger: TimeMessenger,
    loggerFactory: LoggerFactory,
) : TestSummarySender {

    private val logger = loggerFactory.create<TimeTestSummarySender>()

    private val testSummaryComposer: TestSummaryComposer = TestSummaryComposerImpl(reportViewerUrl)

    override fun send(suite: CrossDeviceSuite, reportId: String) {
        destinations.forEach { destination ->
            val channelId = destination.timeChannelId ?: return@forEach
            val teamSuite = suite.filterTeam(destination.teamName)
            if (teamSuite.crossDeviceRuns.isEmpty()) {
                logger.info("No tests of team ${destination.teamName.name}, nothing to send to Time")
                return@forEach
            }

            testSummaryComposer.composeMessage(
                testData = teamSuite,
                team = destination.teamName,
                reportCoordinates = reportCoordinates,
                reportId = reportId,
                buildUrl = buildUrl
            ).flatMap { message ->
                timeMessenger.createPost(channelId = channelId, message = message)
            }.onFailure { error ->
                logger.critical("Cannot send test summary of team ${destination.teamName.name} to Time", error)
            }
        }
    }
}
