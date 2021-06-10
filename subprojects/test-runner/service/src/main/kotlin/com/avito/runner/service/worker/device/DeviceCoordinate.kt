package com.avito.runner.service.worker.device

sealed class DeviceCoordinate {

    abstract val serial: Serial

    data class Kubernetes(
        override val serial: Serial.Remote,
        val podName: String
    ) : DeviceCoordinate()

    data class Local(override val serial: Serial.Local) : DeviceCoordinate() {

        companion object
    }
}
