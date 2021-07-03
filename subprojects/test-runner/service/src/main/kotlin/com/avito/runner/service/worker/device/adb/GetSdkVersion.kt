package com.avito.runner.service.worker.device.adb

import com.avito.android.Result
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.listener.AdbDeviceGetSdkListener
import com.avito.utils.ProcessRunner
import java.time.Duration

internal class GetSdkVersion(
    private val processRunner: ProcessRunner,
    private val retryAction: RetryAction,
    private val adb: Adb,
    private val eventsListener: AdbDeviceGetSdkListener,
) {

    fun get(serial: Serial): Result<Int> {
        return retryAction.retry(
            retriesCount = 3,
            delaySeconds = 5,
            action = {
                processRunner.run(
                    command = "${adb.adbPath} -s ${serial.value} shell getprop ro.build.version.sdk",
                    timeout = Duration.ofSeconds(5)
                ).map { it.toInt() }.getOrThrow()
            },
            onError = { attempt, _, durationMs ->
                eventsListener.onGetSdkPropertyError(attempt, durationMs)
            },
            onFailure = { throwable, durationMs ->
                eventsListener.onGetSdkPropertyFailure(throwable, durationMs)
            },
            onSuccess = { attempt, result, durationMs ->
                eventsListener.onGetSdkPropertySuccess(attempt, result, durationMs)
            }
        )
    }
}
