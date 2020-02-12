package com.avito.runner.test.mock

import com.avito.runner.logging.Logger
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunAction
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.model.getData
import com.avito.runner.service.worker.model.DeviceInstallation
import com.avito.runner.service.worker.model.Installation
import com.google.common.truth.Truth.assertWithMessage
import org.funktionale.tries.Try
import java.io.File
import java.nio.file.Path
import java.util.ArrayDeque
import java.util.Queue

class MockDevice(
    override val id: String,
    private val logger: Logger,
    installApplicationResults: List<MockActionResult<Any>> = emptyList(),
    gettingDeviceStatusResults: List<MockActionResult<Device.DeviceStatus>> = emptyList(),
    runTestsResults: List<MockActionResult<TestCaseRun.Result>> = emptyList(),
    clearPackageResults: List<MockActionResult<Try<Any>>> = emptyList(),
    override val online: Boolean = true,
    override val api: Int = 22
) : Device {

    private val installApplicationResultsQueue: Queue<MockActionResult<Any>> =
        ArrayDeque(installApplicationResults)
    private val gettingDeviceStatusResultsQueue: Queue<MockActionResult<Device.DeviceStatus>> =
        ArrayDeque(gettingDeviceStatusResults)
    private val runTestsResultsQueue: Queue<MockActionResult<TestCaseRun.Result>> =
        ArrayDeque(runTestsResults)
    private val clearPackageResultsQueue: Queue<MockActionResult<Try<Any>>> =
        ArrayDeque(clearPackageResults)

    override fun installApplication(application: String): DeviceInstallation {
        if (installApplicationResultsQueue.isEmpty()) {
            throw IllegalArgumentException(
                "Install application results queue is empty in MockDevice"
            )
        }

        installApplicationResultsQueue.poll().get()

        return DeviceInstallation(
            installation = Installation(
                application = application,
                timestampStartedMilliseconds = 0,
                timestampCompletedMilliseconds = 0
            ),
            device = this.getData()
        )
    }

    override fun runIsolatedTest(
        action: InstrumentationTestRunAction,
        outputDir: File
    ): DeviceTestCaseRun {
        if (runTestsResultsQueue.isEmpty()) {
            throw IllegalArgumentException(
                "Running test results queue is empty in MockDevice"
            )
        }

        val result = runTestsResultsQueue.poll().get()

        return DeviceTestCaseRun(
            testCaseRun = TestCaseRun(
                test = action.test,
                result = result,
                timestampStartedMilliseconds = 0,
                timestampCompletedMilliseconds = 0
            ),
            device = this.getData()
        )
    }

    override fun clearPackage(name: String): Try<Any> {
        if (clearPackageResultsQueue.isEmpty()) {
            throw IllegalArgumentException(
                "Clear package results queue is empty in MockDevice"
            )
        }

        return clearPackageResultsQueue.poll().get()
    }

    override fun pull(from: Path, to: Path): Try<Any> = Try {}

    override fun clearDirectory(remotePath: Path): Try<Any> = Try {}

    override fun list(remotePath: String): Try<Any> = Try {}

    override fun deviceStatus(): Device.DeviceStatus {
        if (gettingDeviceStatusResultsQueue.isEmpty()) {
            throw IllegalArgumentException(
                "Getting device status results queue is empty in MockDevice"
            )
        }

        return gettingDeviceStatusResultsQueue.poll().get()
    }

    override fun log(message: String) {
        logger.log(message)
    }

    override fun notifyError(message: String, error: Throwable) {
        logger.notify(message, error)
    }

    fun verify() {
        assertWithMessage("Mock device has remains commands in queue: installApplicationResultsQueue")
            .that(installApplicationResultsQueue)
            .isEmpty()

        assertWithMessage("Mock device has remains commands in queue: gettingDeviceStatusResultsQueue")
            .that(gettingDeviceStatusResultsQueue)
            .isEmpty()

        assertWithMessage("Mock device has remains commands in queue: runTestsResultsQueue")
            .that(runTestsResultsQueue)
            .isEmpty()

        assertWithMessage("Mock device has remains commands in queue: clearPackageResultsQueue")
            .that(clearPackageResultsQueue)
            .isEmpty()
    }
}
