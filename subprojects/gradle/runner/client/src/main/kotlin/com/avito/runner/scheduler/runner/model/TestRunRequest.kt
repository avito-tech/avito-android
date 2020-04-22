package com.avito.runner.scheduler.runner.model

import com.avito.runner.service.model.TestCase
import com.avito.runner.service.worker.device.model.DeviceConfiguration

data class TestRunRequest(
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
    )
}
