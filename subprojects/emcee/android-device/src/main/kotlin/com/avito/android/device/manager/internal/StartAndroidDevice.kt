package com.avito.android.device.manager.internal

import com.avito.android.device.AndroidDevice
import com.avito.android.device.DeviceSerial
import com.avito.android.device.avd.internal.command.StartAvdCommand
import com.avito.android.device.internal.AndroidDeviceImpl
import com.avito.android.device.internal.InstallApplicationCommand
import com.avito.android.device.manager.internal.storage.AndroidDeviceCoordinates
import com.avito.android.device.manager.internal.storage.AndroidDeviceProcess
import com.avito.android.device.manager.internal.storage.AndroidDeviceProcessStorage
import com.avito.cli.Notification
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.request.device.AsyncDeviceMonitorRequest
import com.malinskiy.adam.request.device.DeviceState
import com.malinskiy.adam.request.prop.GetSinglePropRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ExecutorService
import java.util.logging.Logger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
internal class StartAndroidDevice(
    private val adb: AndroidDebugBridgeClient,
    private val startAvdCommand: StartAvdCommand,
    private val executorService: ExecutorService,
    private val storage: AndroidDeviceProcessStorage,
) {

    private val logger = Logger.getLogger("StartAndroidDevice")

    suspend fun execute(sdk: Int, type: String): AndroidDevice = coroutineScope {

        val deviceStatusDeferred = async {
            val channel = adb.execute(AsyncDeviceMonitorRequest(), this)
            val device = channel.consumeAsFlow()
                .map { it.firstOrNull() }
                .filterNotNull()
                .filter { it.state == DeviceState.DEVICE }
                .first()
            channel.cancel()
            device
        }

        val avdProcessFuture = executorService.submit(startAvd(sdk, type))
        val device = deviceStatusDeferred.await()

        storage.put(AndroidDeviceCoordinates(sdk, type), AndroidDeviceProcess(avdProcessFuture))

        return@coroutineScope AndroidDeviceImpl(
            sdk = sdk,
            type = type,
            serial = DeviceSerial(device.serial),
            adb = adb,
            installApplicationCommand = InstallApplicationCommand(adb),
        ).apply { prepareDevice(this) }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun prepareDevice(device: AndroidDevice) {
        logger.info("Waiting for device...")
        var result: String
        do {
            delay(100.milliseconds)
            result = adb.execute(
                GetSinglePropRequest("sys.boot_completed"),
                serial = device.serial.value,
            ).trimIndent()
        } while (result != "1")
        logger.info("Device is booted and ready to work")
    }

    private fun startAvd(sdk: Int, type: String): Runnable = kotlinx.coroutines.Runnable {
        runBlocking {
            // This Flow is infinite, so cannot be collected in coroutineScope
            startAvdCommand.execute(sdk, type)
                .takeWhile { it !is Notification.Exit }
                .collect { notification ->
                    when (notification) {
                        is Notification.Output -> logger.fine(notification.line)
                        is Notification.Exit -> logger.fine(notification.output)
                    }
                }
        }
    }
}
