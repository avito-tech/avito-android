package com.avito.android.runner.devices.internal

import com.avito.android.Result
import com.avito.runner.service.worker.device.Serial
import java.io.File

internal interface Device {

    val serial: Serial

    fun redirectLogcatToFile(
        file: File,
        tags: Collection<String>
    )

    suspend fun waitForBoot(): Result<String>
}
