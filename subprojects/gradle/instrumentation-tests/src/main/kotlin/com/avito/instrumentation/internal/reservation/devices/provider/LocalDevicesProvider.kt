package com.avito.instrumentation.internal.reservation.devices.provider

import com.avito.instrumentation.internal.reservation.adb.AndroidDebugBridge
import com.avito.instrumentation.internal.reservation.adb.EmulatorsLogsReporter
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.runner.service.worker.device.adb.AdbDevice
import com.avito.runner.service.worker.device.adb.AdbDevicesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.distinctBy
import kotlinx.coroutines.channels.take
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class LocalDevicesProvider(
    private val androidDebugBridge: AndroidDebugBridge,
    private val devicesManager: AdbDevicesManager,
    private val emulatorsLogsReporter: EmulatorsLogsReporter,
    private val adb: Adb,
    private val loggerFactory: LoggerFactory
) : DevicesProvider {

    private val logger = loggerFactory.create<LocalDevicesProvider>()

    private val devices = Channel<Device>(Channel.UNLIMITED)

    private val adbQueryIntervalMs = 5000L

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
                        val adbDevices = findDevices(reservation, acquiredCoordinates)

                        logger.debug("Found local devices: $adbDevices")

                        adbDevices.forEach { device ->
                            val coordinate = device.coordinate
                            check(coordinate is DeviceCoordinate.Local)
                            emulatorsLogsReporter.redirectLogcat(
                                emulatorName = coordinate.serial,
                                device = androidDebugBridge.getLocalDevice(coordinate.serial)
                            )
                            devices.send(device)
                            acquiredCoordinates.add(coordinate)
                        }
                        delay(adbQueryIntervalMs)
                    } while (!devices.isClosedForSend && acquiredCoordinates.size != devicesRequired)
                }
            }
        }
        // todo use flow
        @Suppress("DEPRECATION")
        return devices.distinctBy { it.coordinate }.take(devicesRequired)
    }

    override suspend fun releaseDevice(coordinate: DeviceCoordinate, scope: CoroutineScope) {
        // empty
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
                        loggerFactory = loggerFactory,
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
            logger.warn("Failed to get local emulators", t)
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
