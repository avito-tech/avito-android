package com.avito.runner.service.worker.device

public sealed class DeviceCoordinate {

    public abstract val serial: Serial

    public data class Kubernetes(
        override val serial: Serial.Remote,
        val podName: String
    ) : DeviceCoordinate()

    public data class Local(override val serial: Serial.Local) : DeviceCoordinate() {

        public companion object
    }
}
