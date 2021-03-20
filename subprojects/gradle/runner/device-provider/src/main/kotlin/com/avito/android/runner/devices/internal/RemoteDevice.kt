package com.avito.android.runner.devices.internal

import com.avito.android.Result
import com.avito.runner.service.worker.device.Serial

internal interface RemoteDevice : Device {
    override val serial: Serial.Remote
    fun disconnect(): Result<String>
    fun connect(): Result<String>
}
