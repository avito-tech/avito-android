package com.avito.instrumentation.reservation.devices.provider

import com.avito.instrumentation.reservation.adb.AndroidDebugBridge
import com.avito.instrumentation.reservation.adb.EmulatorsLogsReporter
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDevice
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import com.avito.utils.logging.CILogger
import com.avito.utils.logging.commonLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.distinctBy
import kotlinx.coroutines.channels.take
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class LocalDevicesProvider(
    private val androidDebugBridge: AndroidDebugBridge,
    private val devicesManager: AdbDevicesManager,
    private val emulatorsLogsReporter: EmulatorsLogsReporter,
    private val adb: Adb,
    private val logger: CILogger
) : DevicesProvider {

    private val devices = Channel<Device>(Channel.UNLIMITED)

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    override fun provideFor(reservations: Collection<Reservation.Data>, scope: CoroutineScope): ReceiveChannel<Device> {
        val devicesRequired = reservations.fold(0, { acc, reservation -> acc + reservation.count })
        scope.launch(Dispatchers.IO) {
            reservations.forEach { reservation ->
                check(reservation.device is com.avito.instrumentation.reservation.request.Device.LocalEmulator) {
                    "Non-local emulator ${reservation.device} is unsupported in local reservation"
                }
                launch {
                    do {
                        val acquiredCoordinates = mutableSetOf<DeviceCoordinate>()
                        val findDevices = findDevices(reservation, acquiredCoordinates)
                        findDevices.forEach { device ->
                            val coordinate = device.coordinate
                            check(coordinate is DeviceCoordinate.Local)
                            emulatorsLogsReporter.redirectLogcat(
                                emulatorName = coordinate.serial,
                                device = androidDebugBridge.getLocalDevice(coordinate.serial)
                            )
                            devices.send(device)
                            acquiredCoordinates.add(coordinate)
                        }
                        delay(TimeUnit.SECONDS.toMillis(5))
                    } while (!devices.isClosedForSend && acquiredCoordinates.size != devicesRequired)
                    logger.info("Find local device")
                }
            }
        }
        return devices.distinctBy { it.coordinate }.take(devicesRequired)
    }

    private fun findDevices(
        reservation: Reservation.Data,
        acquiredDevices: Set<DeviceCoordinate>
    ): Set<Device> {
        return try {
            logger.debug("Getting local emulators")
            val devices = devicesManager.connectedDevices()
                .asSequence()
                .filter { it.id is Serial.Local }
                .map { adbDeviceParams ->
                    AdbDevice(
                        coordinate = DeviceCoordinate.Local(adbDeviceParams.id as Serial.Local),
                        model = adbDeviceParams.model,
                        online = adbDeviceParams.online,
                        logger = commonLogger(logger),
                        adb = adb
                    )
                }
                .filter { !acquiredDevices.contains(it.coordinate) }
                .filter { fitsReservation(it, reservation) }
                .filter { isBooted(it) }
                .toSet()
            logger.debug(
                "Getting local emulators completed. " +
                    "Received ${devices.size} emulators."
            )
            devices.toSet()
        } catch (t: Throwable) {
            logger.debug("Failed to get local emulators", t)
            emptySet()
        }
    }

    // TODO blocking operation
    private fun isBooted(device: Device) = device.deviceStatus() == Device.DeviceStatus.Alive

    private fun fitsReservation(device: Device, reservation: Reservation.Data) =
        device.online && device.api == reservation.device.api

    override suspend fun releaseDevices() {
        devices.close()
    }
}
