package com.avito.test.summary.sender

import com.avito.report.model.TestStatus
import com.avito.reportviewer.model.SimpleRunTest
import com.avito.test.summary.model.CrossDeviceRunTest
import com.avito.test.summary.model.CrossDeviceStatus
import com.avito.test.summary.model.CrossDeviceSuite
import com.avito.test.summary.model.FailureOnDevice

internal object ToCrossDeviceSuiteConverter {

    fun convert(
        testData: List<SimpleRunTest>
    ): CrossDeviceSuite {
        return testData
            .groupBy { it.name }
            .map { (testName, runs) ->
                val status: CrossDeviceStatus = when {
                    runs.any { it.status is TestStatus.Lost } ->
                        CrossDeviceStatus.LostOnSomeDevices

                    runs.all { it.status is TestStatus.Skipped } ->
                        CrossDeviceStatus.SkippedOnAllDevices

                    runs.all { it.status is TestStatus.Manual } ->
                        CrossDeviceStatus.Manual

                    /**
                     * Успешным прогоном является при соблюдении 2 условий:
                     *  - Все тесты прошли (имеют Success статус)
                     *  - Есть пропущенные тесты (скипнули на каком-то SDK например),
                     *    но все остальные являются успешными (как минимум 1)
                     */
                    runs.any { it.status is TestStatus.Success } &&
                        runs.all { it.status is TestStatus.Success || it.status is TestStatus.Skipped } ->
                        CrossDeviceStatus.Success

                    runs.any { it.status is TestStatus.Failure } &&
                        runs.all { it.status is TestStatus.Failure || it.status is TestStatus.Skipped } ->
                            CrossDeviceStatus.FailedOnAllDevices(runs.deviceFailures())

                    runs.any { it.status is TestStatus.Failure } ->
                        CrossDeviceStatus.FailedOnSomeDevices(runs.deviceFailures())

                    else ->
                        CrossDeviceStatus.Inconsistent
                }
                CrossDeviceRunTest(testName, status)
            }
            .let { CrossDeviceSuite(it) }
    }

    private fun List<SimpleRunTest>.deviceFailures(): List<FailureOnDevice> {
        return this.filter { it.status is TestStatus.Failure }
            .map {
                FailureOnDevice(it.deviceName, (it.status as TestStatus.Failure).verdict)
            }
    }
}
