package com.avito.android.runner.devices.internal

import com.avito.android.Result
import com.avito.android.runner.devices.DevicesProvider
import com.avito.android.runner.devices.model.ReservationData
import com.avito.logger.LoggerFactory
import com.avito.runner.model.TestCaseRun
import com.avito.runner.service.DeviceWorkerPool
import com.avito.runner.service.DeviceWorkerPoolProvider
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.stub.StubActionResult
import com.avito.runner.service.worker.device.stub.StubDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.coroutines.coroutineContext

internal class FakeDevicesProvider(
    private val loggerFactory: LoggerFactory,
    private val deviceWorkerPoolProvider: DeviceWorkerPoolProvider,
    private val devices: Channel<Device>
) : DevicesProvider {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun provideFor(
        reservations: Collection<ReservationData>
    ): DeviceWorkerPool {
        val devicesRequired = reservations.fold(0, { acc, reservation -> acc + reservation.count })
        with(CoroutineScope(coroutineContext)) {
            launch {
                reservations.forEach { reservation ->
                    check(reservation.device is com.avito.instrumentation.reservation.request.Device.MockEmulator) {
                        "Non-mock emulator ${reservation.device} is unsupported in mock reservation"
                    }
                    do {
                        val acquiredCoordinates = mutableSetOf<DeviceCoordinate>()
                        val acquiredDevice = successfulStubDevice(
                            model = reservation.device.model,
                            api = reservation.device.api,
                            loggerFactory = loggerFactory
                        )
                        devices.send(acquiredDevice)
                        acquiredCoordinates.add(acquiredDevice.coordinate)
                    } while (!devices.isClosedForSend && acquiredCoordinates.size != devicesRequired)
                }
            }
        }
        return deviceWorkerPoolProvider.provide(devices)
    }

    override suspend fun releaseDevice(coordinate: DeviceCoordinate) {
        // empty
    }

    override suspend fun releaseDevices() {
        devices.close()
    }

    private suspend fun successfulStubDevice(
        model: String,
        api: Int,
        loggerFactory: LoggerFactory
    ): StubDevice {
        yield()
        return StubDevice(
            loggerFactory = loggerFactory,
            installApplicationResults = generateList { StubDevice.installApplicationSuccess() },
            gettingDeviceStatusResults = generateList { deviceIsAlive() },
            runTestsResults = generateList { testPassed() },
            clearPackageResults = generateList { succeedClearPackage() },
            model = model,
            apiResult = StubActionResult.Success(api)
        )
    }

    private fun <T> generateList(size: Int = 10, factory: () -> T): List<T> {
        val result = mutableListOf<T>()
        repeat(size) {
            result.add(factory())
        }
        return result
    }

    private fun deviceIsAlive(): Device.DeviceStatus {
        return Device.DeviceStatus.Alive
    }

    private fun testPassed(): StubActionResult.Success<TestCaseRun.Result> {
        return StubActionResult.Success(
            TestCaseRun.Result.Passed
        )
    }

    private fun succeedClearPackage(): StubActionResult.Success<Result<Unit>> {
        return StubActionResult.Success(Result.Success(Unit))
    }
}
