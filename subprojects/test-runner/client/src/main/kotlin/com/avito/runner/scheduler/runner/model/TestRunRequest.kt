package com.avito.runner.scheduler.runner.model

import com.avito.runner.service.worker.device.model.DeviceConfiguration
import com.avito.test.model.TestCase

internal data class TestRunRequest(
    val testCase: TestCase,
    val configuration: DeviceConfiguration,
    val application: String?,
    val applicationPackage: String,
    val testApplication: String,
    val testPackage: String,
    val testRunner: String,
    val timeoutMinutes: Long,
    val scheduling: Scheduling,
    val instrumentationParameters: Map<String, String>,
    val enableDeviceDebug: Boolean
) {

    data class Scheduling(
        val retryCount: Int,
        val minimumSuccessCount: Int,
        val minimumFailedCount: Int
    ) {
        companion object
    }

    companion object
}
