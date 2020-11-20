package com.avito.runner.service.worker.device.adb

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.DdmPreferences
import com.android.ddmlib.IDevice
import com.avito.logger.Logger
import com.avito.runner.CommandLineExecutor
import com.avito.runner.ProcessNotification
import com.avito.runner.retry
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunAction
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.DeviceCoordinate
import com.avito.runner.service.worker.device.adb.instrumentation.InstrumentationTestCaseRunParser
import com.avito.runner.service.worker.device.model.getData
import com.avito.runner.service.worker.model.DeviceInstallation
import com.avito.runner.service.worker.model.Installation
import com.avito.runner.service.worker.model.InstrumentationTestCaseRun
import org.funktionale.tries.Try
import rx.Observable
import rx.Single
import java.io.File
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeUnit

data class AdbDevice(
    override val coordinate: DeviceCoordinate,
    override val model: String,
    override val online: Boolean,
    private val adb: Adb,
    private val logger: Logger,
    private val commandLine: CommandLineExecutor = CommandLineExecutor.Impl(),
    private val instrumentationParser: InstrumentationTestCaseRunParser = InstrumentationTestCaseRunParser.Impl()
) : Device {

    // MBS-8531: don't use ADB here to avoid possible recursion
    private val tag: String = "[${coordinate.serial}]"

    override val api: Int by lazy {
        retry(
            retriesCount = 5,
            delaySeconds = 3,
            block = { attempt ->
                logger.debug("Attempt $attempt: getting ro.build.version.sdk")
                loadProperty(
                    key = "ro.build.version.sdk",
                    cast = { it.toInt() }
                )
            },
            attemptFailedHandler = { attempt, _ ->
                logger.debug("Attempt $attempt: reading ro.build.version.sdk failed")
            },
            actionFailedHandler = { throwable ->
                logger.warn("Failed reading ro.build.version.sdk", throwable)
            }
        )
    }

    override fun installApplication(
        application: String
    ): DeviceInstallation {
        val adbDevice = getAdbDevice()

        val started = System.currentTimeMillis()

        retry(
            retriesCount = 10,
            delaySeconds = 5,
            block = { attempt ->
                logger.debug("Attempt $attempt: installing application $application")
                adbDevice.installPackage(application, true)
                logger.debug("Attempt $attempt: application $application installed")
            },
            attemptFailedHandler = { attempt, _ ->
                logger.debug("Attempt $attempt: failed to install application $application")
            },
            actionFailedHandler = { throwable ->
                logger.warn("Failed installing application $application", throwable)
            }
        )

        return DeviceInstallation(
            installation = Installation(
                application = application,
                timestampStartedMilliseconds = started,
                timestampCompletedMilliseconds = System.currentTimeMillis()
            ),
            device = this.getData()
        )
    }

    override fun runIsolatedTest(
        action: InstrumentationTestRunAction,
        outputDir: File
    ): DeviceTestCaseRun {

        val finalInstrumentationArguments = action.instrumentationParams.plus(
            "class" to "${action.test.className}#${action.test.methodName}"
        )

        return runTest(
            test = action.test,
            testPackageName = action.testPackage,
            testRunnerClass = action.testRunner,
            instrumentationArguments = finalInstrumentationArguments,
            outputDir = outputDir,
            timeoutMinutes = action.timeoutMinutes,
            enableDeviceDebug = action.enableDeviceDebug
        )
            .map {
                when (it) {
                    is InstrumentationTestCaseRun.CompletedTestCaseRun -> {
                        DeviceTestCaseRun(
                            testCaseRun = TestCaseRun(
                                test = TestCase(
                                    className = it.className,
                                    methodName = it.name,
                                    deviceName = action.test.deviceName
                                ),
                                result = it.result,
                                timestampStartedMilliseconds = it.timestampStartedMilliseconds,
                                timestampCompletedMilliseconds = it.timestampCompletedMilliseconds
                            ),
                            device = this.getData()
                        )
                    }
                    is InstrumentationTestCaseRun.FailedOnStartTestCaseRun -> {
                        DeviceTestCaseRun(
                            testCaseRun = TestCaseRun(
                                test = action.test,
                                result = TestCaseRun.Result.Failed.InfrastructureError(
                                    errorMessage = "Failed on start test case: ${it.message}"
                                ),
                                timestampStartedMilliseconds = System.currentTimeMillis(),
                                timestampCompletedMilliseconds = System.currentTimeMillis()
                            ),
                            device = this.getData()
                        )
                    }
                    is InstrumentationTestCaseRun.FailedOnInstrumentationParsing -> {
                        DeviceTestCaseRun(
                            testCaseRun = TestCaseRun(
                                test = action.test,
                                result = TestCaseRun.Result.Failed.InfrastructureError(
                                    errorMessage = "Failed on instrumentation parsing: ${it.message}",
                                    cause = it.throwable
                                ),
                                timestampStartedMilliseconds = System.currentTimeMillis(),
                                timestampCompletedMilliseconds = System.currentTimeMillis()
                            ),
                            device = this.getData()
                        )
                    }
                }
            }
            .toBlocking()
            .value()
    }

    override fun deviceStatus(): Device.DeviceStatus = try {
        retry(
            retriesCount = 15,
            delaySeconds = 5,
            block = { attempt ->
                logger.debug("Attempt $attempt: getting boot_completed param")
                val bootCompleted: Boolean = loadProperty(
                    key = "sys.boot_completed",
                    cast = { output -> output == "1" }
                )
                logger.debug("Attempt $attempt: boot_completed param is $bootCompleted")

                if (!bootCompleted) {
                    // TODO it's hard to throw exception on each retry
                    throw IllegalStateException("sys.boot_completed isn't '1'")
                }

                Device.DeviceStatus.Alive
            },
            attemptFailedHandler = { attempt, _ ->
                logger.debug("Attempt $attempt: failed to determine the device status")
            },
            actionFailedHandler = { throwable ->
                logger.warn("Failed reading device status", throwable)
            }
        )
    } catch (t: Throwable) {
        Device.DeviceStatus.Freeze(
            reason = t
        )
    }

    override fun clearPackage(name: String): Try<Any> = Try {
        retry(
            retriesCount = 10,
            delaySeconds = 2,
            block = { attempt ->
                executeBlockingShellCommand(
                    command = listOf("pm", "clear", name)
                )
                logger.debug("Attempt: $attempt: clear package $name completed")
            },
            attemptFailedHandler = { attempt, _ ->
                logger.debug("Attempt $attempt: failed to clear package $name")
            },
            actionFailedHandler = { throwable ->
                logger.warn("Failed clearing package $name", throwable)
            }
        )
    }

    override fun pull(from: Path, to: Path): Try<Any> = Try {
        retry(
            retriesCount = 5,
            delaySeconds = 3,
            block = {
                executeBlockingCommand(
                    command = listOf(
                        "pull",
                        from.toString(),
                        to.toString()
                    )
                )

                val resultFile = File(
                    to.toFile(),
                    from.fileName.toString()
                )

                if (!resultFile.exists()) {
                    // TODO it's overhead throw exception on each retry
                    throw RuntimeException(
                        "Failed to pull file from ${from.toAbsolutePath()} to ${to.toAbsolutePath()}. " +
                            "Result file: ${resultFile.absolutePath} not found."
                    )
                }
            },
            attemptFailedHandler = { attempt, _ ->
                logger.debug("Attempt $attempt: failed to pull $from")
            },
            actionFailedHandler = { throwable ->
                logger.warn("Failed pulling data $from", throwable)
            }
        )
    }

    override fun clearDirectory(remotePath: Path): Try<Any> = Try {
        retry(
            retriesCount = 5,
            delaySeconds = 3,
            block = {
                executeBlockingShellCommand(
                    command = listOf(
                        "rm",
                        "-rf",
                        remotePath.toString()
                    )
                )
            },
            attemptFailedHandler = { attempt, _ ->
                logger.debug("Attempt $attempt: failed to clear package $remotePath")
            },
            actionFailedHandler = { throwable ->
                logger.warn("Failed clearing directory $remotePath", throwable)
            }
        )
    }

    override fun list(remotePath: String): Try<Any> = Try {
        retry(
            retriesCount = 5,
            delaySeconds = 3,
            block = {
                executeBlockingShellCommand(
                    command = listOf(
                        "ls",
                        remotePath
                    )
                )
            },
            attemptFailedHandler = { attempt, _ ->
                logger.debug("Attempt $attempt: failed to list directory $remotePath")
            },
            actionFailedHandler = { throwable ->
                logger.warn("Failed listing path $remotePath", throwable)
            }
        )
    }

    private fun runTest(
        test: TestCase,
        testPackageName: String,
        testRunnerClass: String,
        instrumentationArguments: Map<String, String>,
        outputDir: File,
        timeoutMinutes: Long,
        enableDeviceDebug: Boolean
    ): Single<InstrumentationTestCaseRun> {
        val logsDir = File(File(outputDir, "logs"), coordinate.serial.value)
            .apply { mkdirs() }
        val started = System.currentTimeMillis()

        val output = executeShellCommand(
            command = listOf(
                "am",
                "instrument",
                "-w", // wait for instrumentation to finish before returning.  Required for test runners.
                "-r", // raw mode is necessary for parsing
                "-e debug $enableDeviceDebug",
                instrumentationArguments.formatInstrumentationOptions(),
                "$testPackageName/$testRunnerClass"
            ),
            redirectOutputTo = File(logsDir, "instrumentation-${test.className}#${test.methodName}.txt")
        ).ofType(ProcessNotification.Output::class.java)

        return instrumentationParser
            .parse(output)
            .timeout(
                timeoutMinutes,
                TimeUnit.MINUTES,
                Observable.just(
                    InstrumentationTestCaseRun.CompletedTestCaseRun(
                        className = test.className,
                        name = test.methodName,
                        result = TestCaseRun.Result.Failed.InfrastructureError(
                            "Failed on Timeout"
                        ),
                        timestampStartedMilliseconds = started,
                        timestampCompletedMilliseconds = started + TimeUnit.MINUTES.toMillis(timeoutMinutes)
                    )
                )
            )
            .first()
            .toSingle()
    }

    private fun getAdbDevice(): IDevice {
        AndroidDebugBridge.initIfNeeded(false)
        DdmPreferences.setTimeOut(
            Duration.ofSeconds(DDMLIB_SOCKET_TIME_OUT_SECONDS).toMillis().toInt()
        )

        val bridge = AndroidDebugBridge.createBridge(adb.adbPath, false)
        waitForAdb(bridge)

        return bridge.devices.find {
            it.serialNumber == coordinate.serial.value
        } ?: throw RuntimeException("Device $coordinate not found")
    }

    private fun waitForAdb(
        adb: AndroidDebugBridge,
        timeOut: Duration = Duration.ofMinutes(WAIT_FOR_ADB_TIME_OUT_MINUTES)
    ) {
        var timeOutMs = timeOut.toMillis()
        val sleepTimeMs = TimeUnit.SECONDS.toMillis(1)

        while (!adb.hasInitialDeviceList() && timeOutMs > 0) {
            try {
                Thread.sleep(sleepTimeMs)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }

            timeOutMs -= sleepTimeMs
        }

        if (timeOutMs <= 0 && !adb.hasInitialDeviceList()) {
            throw RuntimeException("Timeout getting device list.", null)
        }
    }

    private fun Map<String, String>.formatInstrumentationOptions(): String = when (isEmpty()) {
        true -> ""
        false -> " " + entries.joinToString(separator = " ") { "-e ${it.key} ${it.value}" }
    }

    private inline fun <reified T> loadProperty(
        key: String,
        crossinline cast: (result: String) -> T
    ): T {
        val commandResult = executeBlockingShellCommand(
            command = listOf("getprop", key)
        )

        val output = commandResult.output.trim()

        return try {
            cast(output)
        } catch (e: Exception) {
            throw RuntimeException("Failed to cast property result with key: $key. Output: $output.")
        }
    }

    private fun executeBlockingShellCommand(
        command: List<String>,
        timeoutSeconds: Long = DEFAULT_COMMAND_TIMEOUT_SECONDS
    ): ProcessNotification.Exit = executeBlockingCommand(
        command = listOf("shell") + command,
        timeoutSeconds = timeoutSeconds
    )

    private fun executeBlockingCommand(
        command: List<String>,
        timeoutSeconds: Long = DEFAULT_COMMAND_TIMEOUT_SECONDS
    ): ProcessNotification.Exit = executeCommand(
        command = command
    )
        .ofType(ProcessNotification.Exit::class.java)
        .timeout(
            timeoutSeconds,
            TimeUnit.SECONDS,
            Observable.error(
                RuntimeException("Timeout: $timeoutSeconds seconds. Failed to execute command: $command on device $coordinate")
            )
        )
        .toBlocking()
        .first()

    private fun executeShellCommand(
        command: List<String>,
        redirectOutputTo: File? = null
    ): Observable<ProcessNotification> = executeCommand(
        command = listOf("shell") + command,
        redirectOutputTo = redirectOutputTo
    )

    private fun executeCommand(
        command: List<String>,
        redirectOutputTo: File? = null
    ): Observable<ProcessNotification> =
        commandLine.executeProcess(
            command = adb.adbPath,
            args = listOf("-s", coordinate.serial.value) + command,
            output = redirectOutputTo
        )

    override fun debug(message: String) {
        logger.debug("$tag $message")
    }

    override fun info(message: String) {
        logger.info(message)
    }

    override fun warn(message: String, error: Throwable?) {
        logger.warn("$tag $message", error)
    }

    override fun toString(): String {
        return "Device $tag"
    }
}

private const val DEFAULT_COMMAND_TIMEOUT_SECONDS = 5L
private const val DDMLIB_SOCKET_TIME_OUT_SECONDS = 20L
private const val WAIT_FOR_ADB_TIME_OUT_MINUTES = 1L
