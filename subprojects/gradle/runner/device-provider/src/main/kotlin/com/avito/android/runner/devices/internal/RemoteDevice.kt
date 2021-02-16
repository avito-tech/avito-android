package com.avito.android.runner.devices.internal

import com.avito.runner.service.worker.device.Serial
import org.funktionale.tries.Try

internal interface RemoteDevice : Device {
    override val serial: Serial.Remote
    fun disconnect(): Try<String>
    fun connect(): Try<String>
}
