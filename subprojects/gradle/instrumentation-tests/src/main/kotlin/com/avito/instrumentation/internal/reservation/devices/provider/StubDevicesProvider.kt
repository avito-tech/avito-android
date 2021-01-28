package com.avito.instrumentation.internal.reservation.devices.provider

import com.avito.instrumentation.reservation.request.Device.MockEmulator
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.logger.LoggerFactory
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.test.StubActionResult
import com.avito.runner.test.StubDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import org.funktionale.tries.Try

internal class StubDevicesProvider(private val loggerFactory: LoggerFactory) : DevicesProvider {

    private val devices = Channel<Device>(Channel.UNLIMITED)

    @ExperimentalCoroutinesApi
    override fun provideFor(reservations: Collection<Reservation.Data>, scope: CoroutineScope): ReceiveChannel<Device> {
        val devicesRequired = reservations.fold(0, { acc, reservation -> acc + reservation.count })
        scope.launch(Dispatchers.IO) {
            reservations.forEach { reservation ->
                check(reservation.device is MockEmulator) {
                    "Non-mock emulator ${reservation.device} is unsupported in mock reservation"
                }
                launch {
                    do {
                        val acquiredCoordinates = mutableSetOf<DeviceCoordinate>()
                        val acquiredDevice = successfulMockDevice(
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
        return devices
    }

    override suspend fun releaseDevice(coordinate: DeviceCoordinate, scope: CoroutineScope) {
        // empty
    }

    override suspend fun releaseDevices() {
        devices.close()
    }

    private fun successfulMockDevice(model: String, api: Int, loggerFactory: LoggerFactory) =
        StubDevice(
            loggerFactory = loggerFactory,
            installApplicationResults = generateList { StubActionResult.Success(Any()) },
            gettingDeviceStatusResults = generateList { deviceIsAlive() },
            runTestsResults = generateList { testPassed() },
            clearPackageResults = generateList { succeedClearPackage() },
            model = model,
            apiResult = StubActionResult.Success(api)
        )

    private fun <T> generateList(size: Int = 10, factory: () -> T): List<T> {
        val result = mutableListOf<T>()
        repeat(size) {
            result.add(factory())
        }
        return result
    }

    private fun deviceIsAlive(): StubActionResult.Success<Device.DeviceStatus> {
        return StubActionResult.Success(
            Device.DeviceStatus.Alive
        )
    }

    private fun testPassed(): StubActionResult.Success<TestCaseRun.Result> {
        return StubActionResult.Success(
            TestCaseRun.Result.Passed
        )
    }

    private fun succeedClearPackage(): StubActionResult.Success<Try<Any>> {
        return StubActionResult.Success<Try<Any>>(
            Try.Success(Unit)
        )
    }
}
