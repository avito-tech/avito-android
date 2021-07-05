package com.avito.reportviewer.model

public sealed class CrossDeviceStatus {
    public object Success : CrossDeviceStatus()
    public object SkippedOnAllDevices : CrossDeviceStatus()
    public data class FailedOnAllDevices(
        override val failures: List<FailureOnDevice>
    ) : CrossDeviceStatus(), HasFailures

    public data class FailedOnSomeDevices(
        override val failures: List<FailureOnDevice>
    ) : CrossDeviceStatus(), HasFailures

    public object LostOnSomeDevices : CrossDeviceStatus()
    public object Manual : CrossDeviceStatus()

    /**
     * не смогли определить статус, таких не должно остаться todo remove
     */
    public object Inconsistent : CrossDeviceStatus()
}

public interface HasFailures {
    public val failures: List<FailureOnDevice>
}
