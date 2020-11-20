package com.avito.report.model

sealed class CrossDeviceStatus {
    object Success : CrossDeviceStatus()
    object SkippedOnAllDevices : CrossDeviceStatus()
    data class FailedOnAllDevices(override val failures: List<FailureOnDevice>) : CrossDeviceStatus(), HasFailures
    data class FailedOnSomeDevices(override val failures: List<FailureOnDevice>) : CrossDeviceStatus(), HasFailures
    object LostOnSomeDevices : CrossDeviceStatus()
    object Manual : CrossDeviceStatus()

    /**
     * не смогли определить статус, таких не должно остаться todo remove
     */
    object Inconsistent : CrossDeviceStatus()
}

interface HasFailures {
    val failures: List<FailureOnDevice>
}
