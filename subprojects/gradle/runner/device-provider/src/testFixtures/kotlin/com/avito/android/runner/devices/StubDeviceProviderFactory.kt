package com.avito.android.runner.devices

import com.avito.android.runner.devices.internal.KubernetesDevicesProvider
import com.avito.android.runner.devices.internal.kubernetes.KubernetesReservationClient
import com.avito.android.runner.devices.internal.kubernetes.createStubInstance
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.time.TimeProvider

public fun createKubernetesDeviceProvider(
    adb: Adb,
    loggerFactory: LoggerFactory,
    timeProvider: TimeProvider
): DevicesProvider {
    return KubernetesDevicesProvider(
        client = KubernetesReservationClient.createStubInstance(
            loggerFactory = loggerFactory
        ),
        adbDevicesManager = AdbDevicesManager(
            loggerFactory = loggerFactory,
            adb = adb
        ),
        loggerFactory = loggerFactory,
        adb = adb,
        timeProvider = timeProvider,
        metricsConfig = null
    )
}
