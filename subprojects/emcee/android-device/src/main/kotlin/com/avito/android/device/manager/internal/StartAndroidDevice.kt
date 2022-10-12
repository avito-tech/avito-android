package com.avito.android.device.manager.internal

import com.avito.android.device.AndroidDevice
import com.avito.android.device.DeviceSerial
import com.avito.android.device.avd.internal.StartAvd
import com.avito.android.device.internal.AndroidDeviceImpl
import com.avito.android.device.internal.InstallPackage
import com.avito.cli.Notification
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.request.device.AsyncDeviceMonitorRequest
import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.device.DeviceState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.logging.Logger

@ExperimentalCoroutinesApi
internal class StartAndroidDevice(
    private val adb: AndroidDebugBridgeClient,
    private val startAvd: StartAvd,
    private val maximumRunningDevices: Int = 1
) {

    private val logger = Logger.getLogger("StartAndroidDevice")

    suspend fun execute(sdk: Int, type: String): AndroidDevice {
        return coroutineScope {
            val deviceEventsChannel: ReceiveChannel<List<Device>> = adb.execute(
                request = AsyncDeviceMonitorRequest(),
                scope = this
            )
            try {
                val activeDeviceDeferred = async {
                    waitDevice(deviceEventsChannel, sdk, type)
                }
                val job = launch {
                    startAvd(sdk, type)
                }
                val device = activeDeviceDeferred.await()
                job.cancel()
                device
            } finally {
                logger.info("finally")
                withContext(NonCancellable) {
                    if (!deviceEventsChannel.isClosedForReceive) {
                        deviceEventsChannel.cancel()
                    }
                    cancel()
                }
            }
        }
    }

    private suspend fun startAvd(sdk: Int, type: String) {
        startAvd.execute(sdk, type)
            .collect { notification ->
                when (notification) {
                    is Notification.Exit -> logger.info("start avd exits: \n ${notification.output}")
                    is Notification.Output -> logger.info(notification.line)
                }
            }
    }

    private suspend fun waitDevice(
        deviceEventsChannel: ReceiveChannel<List<Device>>,
        sdk: Int,
        type: String
    ): AndroidDeviceImpl {
        logger.info("Start wait device")
        var device: Device? = null
        for (devices in deviceEventsChannel) {
            logger.info("current device list $devices")
            require(devices.size <= maximumRunningDevices) {
                "Must be maximum $maximumRunningDevices running devices per worker"
            }
            device = devices.singleOrNull { it.state == DeviceState.DEVICE }
            if (device != null) {
                logger.info("Found device: $device")
                break
            }
        }
        return AndroidDeviceImpl(
            sdk = sdk,
            type = type,
            serial = DeviceSerial(device!!.serial),
            adb = adb,
            installPackage = InstallPackage(adb),
        )
    }
}
