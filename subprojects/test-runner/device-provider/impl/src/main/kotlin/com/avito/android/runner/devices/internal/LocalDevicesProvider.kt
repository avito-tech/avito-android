package com.avito.android.runner.devices.internal

import com.avito.android.runner.devices.DevicesProvider
import com.avito.android.runner.devices.model.ReservationData
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.DeviceWorkerPool
import com.avito.runner.service.DeviceWorkerPoolProvider
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.DevicesManager
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.AdbDeviceFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.distinctBy
import kotlinx.coroutines.channels.take
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class LocalDevicesProvider(
    private val androidDebugBridge: AndroidDebugBridge,
    private val emulatorsLogsReporter: EmulatorsLogsReporter,
    private val adbDeviceFactory: AdbDeviceFactory,
    private val devicesManager: DevicesManager,
    private val deviceWorkerPoolProvider: DeviceWorkerPoolProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    loggerFactory: LoggerFactory,
) : DevicesProvider {

    private val logger = loggerFactory.create<LocalDevicesProvider>()

    private val devices = Channel<Device>(Channel.UNLIMITED)

    private val adbQueryIntervalMs = 5000L

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun provideFor(
        reservations: Collection<ReservationData>,
    ): DeviceWorkerPool {
        val devicesRequired = reservations.fold(0, { acc, reservation -> acc + reservation.count })
        with(CoroutineScope(dispatcher)) {
            launch {
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
        }
        @Suppress("DEPRECATION")
        val devices = devices.distinctBy { it.coordinate }.take(devicesRequired)
        return deviceWorkerPoolProvider.provide(devices)
    }

    override suspend fun releaseDevice(coordinate: DeviceCoordinate) {
        // empty
    }

    override suspend fun releaseReservation(name: String) {
    }

    private fun findDevices(
        reservation: ReservationData,
        acquiredDevices: Set<DeviceCoordinate>
    ): Set<Device> {
        return try {
            logger.debug("Getting local emulators")
            val devices = devicesManager.connectedDevices()
                .asSequence()
                .filter { it.id is Serial.Local }
                .map { adbDeviceParams ->
                    adbDeviceFactory.create(
                        coordinate = DeviceCoordinate.Local(adbDeviceParams.id as Serial.Local),
                        adbDeviceParams = adbDeviceParams
                    ).getOrThrow()
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

    private fun fitsReservation(device: Device, reservation: ReservationData) =
        device.online && device.api == reservation.device.api

    override suspend fun releaseDevices() {
        devices.close()
    }
}
