package com.avito.runner.test

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunAction
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.model.getData
import com.avito.runner.service.worker.model.DeviceInstallation
import com.avito.runner.service.worker.model.Installation
import com.google.gson.Gson
import org.funktionale.tries.Try
import java.io.File
import java.nio.file.Path
import java.util.ArrayDeque
import java.util.Date
import java.util.Queue

open class StubDevice(
    override val coordinate: DeviceCoordinate = DeviceCoordinate.Local(Serial.Local("stub")),
    loggerFactory: LoggerFactory,
    installApplicationResults: List<StubActionResult<Any>> = emptyList(),
    gettingDeviceStatusResults: List<StubActionResult<Device.DeviceStatus>> = emptyList(),
    runTestsResults: List<StubActionResult<TestCaseRun.Result>> = emptyList(),
    clearPackageResults: List<StubActionResult<Try<Any>>> = emptyList(),
    private val apiResult: StubActionResult<Int> = StubActionResult.Success(22),
    override val online: Boolean = true,
    override val model: String = "model"
) : Device {

    private val gson = Gson()

    private val installApplicationResultsQueue: Queue<StubActionResult<Any>> =
        ArrayDeque(installApplicationResults)
    private val gettingDeviceStatusResultsQueue: Queue<StubActionResult<Device.DeviceStatus>> =
        ArrayDeque(gettingDeviceStatusResults)
    private val runTestsResultsQueue: Queue<StubActionResult<TestCaseRun.Result>> =
        ArrayDeque(runTestsResults)
    private val clearPackageResultsQueue: Queue<StubActionResult<Try<Any>>> =
        ArrayDeque(clearPackageResults)

    override val logger = loggerFactory.create<StubDevice>()

    override val api: Int
        get() {
            return apiResult.get()
        }

    override fun installApplication(application: String): DeviceInstallation {
        logger.debug("MockDevice: installApplication called")

        check(installApplicationResultsQueue.isNotEmpty()) {
            "Install application results queue is empty in MockDevice"
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
        logger.debug("runIsolatedTest called")

        check(runTestsResultsQueue.isNotEmpty()) {
            "Running test results queue is empty in MockDevice"
        }

        val result = runTestsResultsQueue.poll().get()

        logger.debug("runIsolatedTest resulted with: $result")

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
        logger.debug("clearPackage called")

        check(clearPackageResultsQueue.isNotEmpty()) {
            "Clear package results queue is empty in MockDevice"
        }

        val result = clearPackageResultsQueue.poll().get()

        logger.debug("clearPackage resulted with: $result")

        return result
    }

    override fun pull(from: Path, to: Path): Try<Any> {

        logger.debug("pull called [from: $from to: $to]")

        return if (from.toString().contains("report.json")) {
            to.toFile().writeText("")

            val startTime = Date().time

            val testRuntimeData: TestRuntimeData = TestRuntimeDataPackage(
                incident = null,
                startTime = startTime,
                endTime = startTime + 5000,
                video = null,
                dataSetData = emptyMap(),
                preconditions = emptyList(),
                steps = emptyList()
            )

            val resultFile = File(
                to.toFile(),
                from.fileName.toString()
            )

            resultFile.writeText(gson.toJson(testRuntimeData))

            Try.Success(Any())
        } else {
            Try.Success(Any())
        }
    }

    override fun clearDirectory(remotePath: Path): Try<Any> = Try {}

    override fun list(remotePath: String): Try<Any> = Try {}

    override fun deviceStatus(): Device.DeviceStatus {
        logger.debug("deviceStatus called")

        check(gettingDeviceStatusResultsQueue.isNotEmpty()) {
            "Getting device status results queue is empty in MockDevice"
        }

        val result = gettingDeviceStatusResultsQueue.poll().get()

        logger.debug("deviceStatus resulted with: $result")

        return result
    }

    fun isDone(): Boolean {
        return installApplicationResultsQueue.isEmpty()
            && gettingDeviceStatusResultsQueue.isEmpty()
            && runTestsResultsQueue.isEmpty()
            && clearPackageResultsQueue.isEmpty()
    }

    fun verify() {
        check(installApplicationResultsQueue.isEmpty()) {
            "Mock device has remains commands in queue: installApplicationResultsQueue"
        }
        check(gettingDeviceStatusResultsQueue.isEmpty()) {
            "Mock device has remains commands in queue: gettingDeviceStatusResultsQueue"
        }
        check(runTestsResultsQueue.isEmpty()) {
            "Mock device has remains commands in queue: runTestsResultsQueue"
        }
        check(clearPackageResultsQueue.isEmpty()) {
            "Mock device has remains commands in queue: clearPackageResultsQueue"
        }
    }
}
