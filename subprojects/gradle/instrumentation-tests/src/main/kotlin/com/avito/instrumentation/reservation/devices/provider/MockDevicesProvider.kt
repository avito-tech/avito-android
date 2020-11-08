package com.avito.instrumentation.reservation.devices.provider

import com.avito.instrumentation.reservation.request.Device.MockEmulator
import com.avito.instrumentation.reservation.request.Reservation
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.test.mock.MockActionResult
import com.avito.runner.test.mock.MockDevice
import com.avito.utils.logging.CILogger
import com.avito.utils.logging.commonLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.funktionale.tries.Try
import java.util.concurrent.TimeUnit

class MockDevicesProvider(private val logger: CILogger) : DevicesProvider {

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
                            logger = logger
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

    private fun successfulMockDevice(model: String, api: Int, logger: CILogger) = MockDevice(
        logger = commonLogger(logger),
        installApplicationResults = generateList { MockActionResult.Success(Any()) },
        gettingDeviceStatusResults = generateList { deviceIsAlive() },
        runTestsResults = generateList { testPassed() },
        clearPackageResults = generateList { succeedClearPackage() },
        model = model,
        apiResult = MockActionResult.Success(api)
    )

    private fun <T> generateList(size: Int = 10, factory: () -> T): List<T> {
        val result = mutableListOf<T>()
        repeat(size) {
            result.add(factory())
        }
        return result
    }

    private fun deviceIsAlive(): MockActionResult.Success<Device.DeviceStatus> {
        return MockActionResult.Success(
            Device.DeviceStatus.Alive
        )
    }

    private fun testPassed(): MockActionResult.Success<TestCaseRun.Result> {
        return MockActionResult.Success(
            TestCaseRun.Result.Passed
        )
    }

    private fun succeedClearPackage(): MockActionResult.Success<Try<Any>> {
        return MockActionResult.Success<Try<Any>>(
            Try.Success(Unit)
        )
    }
}
