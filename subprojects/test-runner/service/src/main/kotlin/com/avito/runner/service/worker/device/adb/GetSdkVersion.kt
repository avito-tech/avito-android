package com.avito.runner.service.worker.device.adb

import com.avito.android.Result
import com.avito.android.retry.executeWithRetries
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.listener.AdbDeviceGetSdkListener
import com.avito.utils.ProcessRunner
import java.time.Duration

internal class GetSdkVersion(
    private val processRunner: ProcessRunner,
    private val adb: Adb,
    private val eventsListener: AdbDeviceGetSdkListener,
) {

    fun get(serial: Serial): Result<Int> {
        return executeWithRetries(
            maxAttempts = 3,
            delay = Duration.ofSeconds(5),
            action = {
                processRunner.run(
                    command = "${adb.adbPath} -s ${serial.value} shell getprop ro.build.version.sdk",
                    timeout = Duration.ofSeconds(5)
                ).map { it.toInt() }.getOrThrow()
            },
            onFailedTry = { attempt, _, duration ->
                eventsListener.onGetSdkPropertyError(attempt, duration.toMillis())
            },
            onFailure = { _, throwable, duration ->
                eventsListener.onGetSdkPropertyFailure(throwable, duration.toMillis())
            },
            onSuccess = { attempt, result, duration ->
                eventsListener.onGetSdkPropertySuccess(attempt, result, duration.toMillis())
            }
        )
    }
}
