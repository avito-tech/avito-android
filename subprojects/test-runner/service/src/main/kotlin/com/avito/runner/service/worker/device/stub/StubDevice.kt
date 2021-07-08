package com.avito.runner.service.worker.device.stub

import com.avito.android.Result
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.runner.model.TestCaseRun
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunAction
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.PullValidator
import com.avito.runner.service.worker.device.model.DeviceConfiguration
import com.avito.runner.service.worker.device.model.DeviceData
import com.avito.runner.service.worker.device.model.getData
import com.avito.runner.service.worker.model.DeviceInstallation
import com.avito.runner.service.worker.model.Installation
import com.google.gson.Gson
import java.io.File
import java.nio.file.Path
import java.util.ArrayDeque
import java.util.Date
import java.util.Queue

public open class StubDevice(
    tag: String = "StubDevice",
    override val coordinate: DeviceCoordinate = DeviceCoordinate.Local(Serial.Local("stub")),
    loggerFactory: LoggerFactory,
    installApplicationResults: List<Result<DeviceInstallation>> = emptyList(),
    gettingDeviceStatusResults: List<Device.DeviceStatus> = emptyList(),
    runTestsResults: List<StubActionResult<TestCaseRun.Result>> = emptyList(),
    clearPackageResults: List<StubActionResult<Result<Unit>>> = emptyList(),
    private val apiResult: StubActionResult<Int> = StubActionResult.Success(22),
    override val online: Boolean = true,
    override val model: String = "model"
) : Device {

    private val gson = Gson()

    private val installApplicationResultsQueue: Queue<Result<DeviceInstallation>> =
        ArrayDeque(installApplicationResults)
    private val gettingDeviceStatusResultsQueue: Queue<Device.DeviceStatus> =
        ArrayDeque(gettingDeviceStatusResults)
    private val runTestsResultsQueue: Queue<StubActionResult<TestCaseRun.Result>> =
        ArrayDeque(runTestsResults)
    private val clearPackageResultsQueue: Queue<StubActionResult<Result<Unit>>> =
        ArrayDeque(clearPackageResults)

    override val logger: Logger = loggerFactory.create(tag)

    override val api: Int
        get() = apiResult.get()

    override fun installApplication(applicationPackage: String): Result<DeviceInstallation> {
        resultQueuePrecondition(
            queue = installApplicationResultsQueue,
            functionName = "installApplication",
            values = applicationPackage
        )

        val result = installApplicationResultsQueue.poll()

        logger.debug("installApplication(\"$applicationPackage\") resulted with $result")

        return result
    }

    override fun runIsolatedTest(
        action: InstrumentationTestRunAction,
        outputDir: File
    ): DeviceTestCaseRun {
        resultQueuePrecondition(
            queue = runTestsResultsQueue,
            functionName = "runIsolatedTest",
            values = action.toString()
        )

        val result = runTestsResultsQueue.poll().get()

        logger.debug("runIsolatedTest() resulted with: $result")

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

    override fun clearPackage(name: String): Result<Unit> {
        resultQueuePrecondition(
            queue = clearPackageResultsQueue,
            functionName = "clearPackage",
            values = name
        )

        val result = clearPackageResultsQueue.poll().get()

        logger.debug("clearPackage(\"$name\") resulted with: $result")

        return result
    }

    override fun pull(from: Path, to: Path): Result<File> {

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

            Result.Success(to.toFile())
        } else {
            Result.Success(to.toFile())
        }
    }

    override fun pullDir(deviceDir: Path, hostDir: Path, validator: PullValidator): Result<File> {
        TODO("Not yet implemented")
    }

    override fun clearDirectory(remotePath: Path): Result<Unit> = Result.tryCatch {}

    override fun list(remotePath: String): Result<List<String>> = Result.tryCatch { emptyList() }

    override fun deviceStatus(): Device.DeviceStatus {
        resultQueuePrecondition(
            queue = gettingDeviceStatusResultsQueue,
            functionName = "deviceStatus",
            values = ""
        )

        val result = gettingDeviceStatusResultsQueue.poll()

        logger.debug("deviceStatus() resulted with: $result")

        return result
    }

    override fun logcat(lines: Int?): Result<String> {
        TODO("Not yet implemented")
    }

    public fun isDone(): Boolean {
        return installApplicationResultsQueue.isEmpty()
            && gettingDeviceStatusResultsQueue.isEmpty()
            && runTestsResultsQueue.isEmpty()
            && clearPackageResultsQueue.isEmpty()
    }

    public fun verify() {
        verifyQueueHasNoExcessiveElements(installApplicationResultsQueue, "installApplication")
        verifyQueueHasNoExcessiveElements(gettingDeviceStatusResultsQueue, "deviceStatus")
        verifyQueueHasNoExcessiveElements(runTestsResultsQueue, "runIsolatedTest")
        verifyQueueHasNoExcessiveElements(clearPackageResultsQueue, "clearPackage")
    }

    private fun resultQueuePrecondition(queue: Queue<*>, functionName: String, values: String) {
        if (queue.isEmpty()) {
            val errorMessage = "[TEST-ERROR] $functionName(\"$values\") is called, but it's results queue is empty"
            val exception = IllegalStateException(errorMessage)
            logger.critical(errorMessage, exception)
            throw exception
        }
    }

    private fun verifyQueueHasNoExcessiveElements(queue: Queue<*>, functionName: String) {
        if (queue.isNotEmpty()) {
            val errorMessage = "[TEST-ERROR] $functionName has excessive commands in queue: ${queue.toList()}"
            val exception = IllegalStateException(errorMessage)
            logger.critical(errorMessage, exception)
            throw exception
        }
    }

    public companion object {

        public fun installApplicationSuccess(applicationPackage: String = "doesntmatter"): Result<DeviceInstallation> {
            return Result.Success(
                DeviceInstallation(
                    installation = Installation(
                        application = applicationPackage,
                        timestampStartedMilliseconds = 0,
                        timestampCompletedMilliseconds = 0
                    ),
                    device = DeviceData(
                        serial = Serial.Local(""),
                        configuration = DeviceConfiguration(api = 28, model = "model")
                    )
                )
            )
        }

        public fun installApplicationFailure(): Result<DeviceInstallation> {
            return Result.Failure(Exception())
        }
    }
}
