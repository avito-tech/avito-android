package com.avito.test.summary.compose

import com.avito.report.StubReportViewer
import com.avito.report.model.CrossDeviceRunTest
import com.avito.report.model.CrossDeviceStatus.FailedOnAllDevices
import com.avito.report.model.CrossDeviceStatus.FailedOnSomeDevices
import com.avito.report.model.CrossDeviceStatus.LostOnSomeDevices
import com.avito.report.model.CrossDeviceStatus.Manual
import com.avito.report.model.CrossDeviceStatus.SkippedOnAllDevices
import com.avito.report.model.CrossDeviceStatus.Success
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.FailureOnDevice
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Team
import com.avito.report.model.createStubInstance
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test

internal class SlackSummaryComposerTest {

    private val reportViewer = StubReportViewer()
    private val composer: SlackSummaryComposer = SlackSummaryComposerImpl(reportViewer)

    @Test
    fun `slack message - contains manual tests count`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = Manual),
                CrossDeviceRunTest.createStubInstance(status = Manual)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Try.Success<*>>()
        assertThat(message.get()).contains("Ручные тесты: 2")
    }

    @Test
    fun `slack message - contains automated tests count`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = Success),
                CrossDeviceRunTest.createStubInstance(status = FailedOnAllDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = FailedOnSomeDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = Manual)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Try.Success<*>>()
        assertThat(message.get()).contains("*Автотесты*: 3")
    }

    @Test
    fun `slack message - contains success tests count and percentage`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = Success),
                CrossDeviceRunTest.createStubInstance(status = FailedOnAllDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = Success),
                CrossDeviceRunTest.createStubInstance(status = Success),
                CrossDeviceRunTest.createStubInstance(status = FailedOnSomeDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = Manual)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Try.Success<*>>()

        // не 50% потому что manual не берется в расчет
        assertThat(message.get()).contains(
            ":green_heart: *Зеленые тесты*: 3 (60%)"
        )
    }

    @Test
    fun `slack message - contains skipped tests count and percentage`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = SkippedOnAllDevices),
                CrossDeviceRunTest.createStubInstance(status = SkippedOnAllDevices),
                CrossDeviceRunTest.createStubInstance(status = SkippedOnAllDevices)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Try.Success<*>>()
        assertThat(message.get()).contains(
            ":white_circle: *Пропущенные тесты (например, заигнорен) на всех девайсах*: 3 (100%)"
        )
    }

    @Test
    fun `slack message - contains lost tests count and percentage`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = LostOnSomeDevices),
                CrossDeviceRunTest.createStubInstance(status = LostOnSomeDevices),
                CrossDeviceRunTest.createStubInstance(status = SkippedOnAllDevices),
                CrossDeviceRunTest.createStubInstance(status = SkippedOnAllDevices)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Try.Success<*>>()
        assertThat(message.get()).contains(
            ":black_circle: *Потерянные тесты (например, зависли и не зарепортились) на некоторых девайсах*: 2 (50%)"
        )
    }

    @Test
    fun `slack message - contains all failed tests count and percentage`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = Success),
                CrossDeviceRunTest.createStubInstance(status = FailedOnAllDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = FailedOnAllDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = FailedOnSomeDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = Manual)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Try.Success<*>>()
        assertThat(message.get()).contains(":red_circle: *Тесты упали на всех девайсах*: 2 (50%)")
    }

    @Test
    fun `slack message - contains failed tests on some devices count and percentage`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = Success),
                CrossDeviceRunTest.createStubInstance(status = FailedOnAllDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = FailedOnSomeDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = FailedOnSomeDevices(emptyList())),
                CrossDeviceRunTest.createStubInstance(status = Manual)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Try.Success<*>>()
        assertThat(message.get()).contains(":warning: *Тесты упали только на некоторых девайсах*: 2 (50%)")
    }

    @Test
    fun `slack message - contains failures with count`() {
        val crossDeviceSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(
                    status = FailedOnSomeDevices(
                        listOf(
                            FailureOnDevice("device1", "failure1"),
                            FailureOnDevice("device2", "failure2"),
                            FailureOnDevice("device3", "failure3"),
                            FailureOnDevice("device4", "failure4"),
                            FailureOnDevice("device5", "failure5"),
                            FailureOnDevice("device5", "failure6")
                        )
                    )
                ),
                CrossDeviceRunTest.createStubInstance(
                    status = FailedOnSomeDevices(
                        listOf(
                            FailureOnDevice("device1", "failure1"),
                            FailureOnDevice("device2", "failure2"),
                            FailureOnDevice("device3", "failure3")
                        )
                    )
                ),
                CrossDeviceRunTest.createStubInstance(status = Manual)
            )
        )

        val message = compose(crossDeviceSuite)

        assertThat(message).isInstanceOf<Try.Success<*>>()
        assertThat(message.get()).contains("*2* из-за ```failure1```")
        assertThat(message.get()).contains("*2* из-за ```failure2```")
        assertThat(message.get()).contains("*2* из-за ```failure3```")
        assertThat(message.get()).contains("*1* из-за ```failure4```")
        assertThat(message.get()).contains("*1* из-за ```failure5```")
        assertThat(message.get()).contains("И еще *1*")
    }

    @Test
    fun `slack message - contains @channel - if mention enabled and there are failed tests`() {
        val suiteWithFailure = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(
                    status = FailedOnSomeDevices(listOf(FailureOnDevice("device1", "failure1")))
                ),
                CrossDeviceRunTest.createStubInstance(
                    status = Success
                )
            )
        )

        val message = compose(suiteWithFailure, mentionOnFailures = true)

        assertThat(message).isInstanceOf<Try.Success<*>>()
        assertThat(message.get()).contains("<!channel>")
    }

    @Test
    fun `slack message - does not contain @channel - if mention enabled and there are no failed tests`() {
        val greenSuite = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(status = Success),
                CrossDeviceRunTest.createStubInstance(status = Success)
            )
        )

        val message = compose(greenSuite, mentionOnFailures = true)

        assertThat(message).isInstanceOf<Try.Success<*>>()
        assertThat(message.get()).doesNotContain("<!channel>")
    }

    @Test
    fun `slack message - does not contain @channel - if mention disabled and there are failed tests`() {
        val suiteWithFailure = CrossDeviceSuite(
            listOf(
                CrossDeviceRunTest.createStubInstance(
                    status = FailedOnSomeDevices(listOf(FailureOnDevice("device1", "failure1")))
                ),
                CrossDeviceRunTest.createStubInstance(
                    status = Success
                )
            )
        )

        val message = compose(suiteWithFailure, mentionOnFailures = false)

        assertThat(message).isInstanceOf<Try.Success<*>>()
        assertThat(message.get()).doesNotContain("<!channel>")
    }

    private fun compose(
        testData: CrossDeviceSuite,
        team: Team = Team.UNDEFINED,
        mentionOnFailures: Boolean = false
    ): Try<String> {
        return composer.composeMessage(
            testData = testData,
            team = team,
            mentionOnFailures = mentionOnFailures,
            reportCoordinates = ReportCoordinates("Any", "Any", "Any"),
            reportId = "Any",
            buildUrl = "build_url"
        )
    }
}
