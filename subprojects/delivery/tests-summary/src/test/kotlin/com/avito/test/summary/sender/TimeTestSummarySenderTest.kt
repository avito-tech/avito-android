package com.avito.test.summary.sender

import com.avito.alertino.model.AlertinoRecipient
import com.avito.android.Result
import com.avito.logger.PrintlnLoggerFactory
import com.avito.report.model.Team
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.model.TestName
import com.avito.test.summary.model.CrossDeviceRunTest
import com.avito.test.summary.model.CrossDeviceStatus
import com.avito.test.summary.model.CrossDeviceSuite
import com.avito.test.summary.model.FailureOnDevice
import com.avito.test.summary.model.TestSummaryDestination
import com.avito.test.summary.model.createStubInstance
import com.avito.test.summary.time.TimeMessenger
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TimeTestSummarySenderTest {

    private val timeMessenger = FakeTimeMessenger()

    private val destinations = listOf(
        TestSummaryDestination(Team("undefined"), AlertinoRecipient("#dev-null")),
        TestSummaryDestination(Team("sx"), AlertinoRecipient("#gsx-android-tests"), timeChannelId = "sx-channel"),
        TestSummaryDestination(Team("bx"), AlertinoRecipient("#gbx-android-tests"), timeChannelId = "bx-channel"),
        TestSummaryDestination(Team("comms"), AlertinoRecipient("#gcomms-android-tests")),
    )

    @Test
    fun `send - posts team summary - only for teams with time channel and tests in run`() {
        val suite = CrossDeviceSuite(
            listOf(
                test(team = "sx", method = "openItem", status = CrossDeviceStatus.Success),
                test(
                    team = "sx",
                    method = "buyItem",
                    status = CrossDeviceStatus.FailedOnAllDevices(listOf(FailureOnDevice("api34", "No button"))),
                ),
                test(team = "comms", method = "sendMessage", status = CrossDeviceStatus.Success),
            )
        )

        createSender().send(suite = suite, reportId = "report-id")

        assertThat(timeMessenger.posts.map { it.first }).containsExactly("sx-channel")
        val message = timeMessenger.posts.single().second
        assertThat(message).contains("Юнит: sx")
        assertThat(message).contains("*Автотесты*: 2")
        assertThat(message).contains("*1* из-за ```No button```")
    }

    @Test
    fun `send - continues with other teams - when time rejects post`() {
        timeMessenger.failingChannels += "sx-channel"
        val suite = CrossDeviceSuite(
            listOf(
                test(team = "sx", method = "openItem", status = CrossDeviceStatus.Success),
                test(team = "bx", method = "openFavorites", status = CrossDeviceStatus.Success),
            )
        )

        createSender().send(suite = suite, reportId = "report-id")

        assertThat(timeMessenger.posts.map { it.first }).containsExactly("bx-channel")
    }

    private fun createSender() = TimeTestSummarySender(
        reportViewerUrl = "http://localhost/",
        buildUrl = "build_url",
        reportCoordinates = ReportCoordinates("GlobalAndroid", "FunctionalTestsEmcee", "run-id"),
        destinations = destinations,
        timeMessenger = timeMessenger,
        loggerFactory = PrintlnLoggerFactory,
    )

    private fun test(team: String, method: String, status: CrossDeviceStatus) = CrossDeviceRunTest.createStubInstance(
        name = TestName(className = "com.avito.android.test.$team.feature.SomeTest", methodName = method),
        status = status,
    )

    private class FakeTimeMessenger : TimeMessenger {

        val posts = mutableListOf<Pair<String, String>>()

        val failingChannels = mutableSetOf<String>()

        override fun createPost(channelId: String, message: String): Result<Unit> {
            if (channelId in failingChannels) {
                return Result.Failure(IllegalStateException("Time API responded with 403"))
            }
            posts += channelId to message
            return Result.Success(Unit)
        }
    }
}
