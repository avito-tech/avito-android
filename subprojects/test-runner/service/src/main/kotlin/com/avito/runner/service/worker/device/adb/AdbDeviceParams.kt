package com.avito.runner.service.worker.device.adb

import com.avito.runner.service.worker.device.Serial

public data class AdbDeviceParams(
    val id: Serial,
    val model: String,
    val online: Boolean
)
