package com.avito.runner.service.worker.device.adb

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.DdmPreferences
import com.android.ddmlib.IDevice
import com.avito.runner.CommandLineExecutor
import com.avito.runner.ProcessNotification
import com.avito.runner.logging.Logger
import com.avito.runner.retry
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunAction
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.adb.instrumentation.InstrumentationTestCaseRunParser
import com.avito.runner.service.worker.device.model.getData
import com.avito.runner.service.worker.model.DeviceInstallation
import com.avito.runner.service.worker.model.Installation
import com.avito.runner.service.worker.model.InstrumentationTestCaseRun
import com.avito.utils.getStackTraceString
import org.funktionale.tries.Try
import rx.Observable
import rx.Single
import java.io.File
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeUnit

data class AdbDevice(
    override val id: String,
    override val model: String,
    override val online: Boolean,
    private val adb: String,
    private val logger: Logger,
    private val commandLine: CommandLineExecutor = CommandLineExecutor.Impl(),
    private val instrumentationParser: InstrumentationTestCaseRunParser = InstrumentationTestCaseRunParser.Impl()
) : Device {

    override val api: Int by lazy {
        loadProperty(
            key = "ro.build.version.sdk",
            cast = { it.toInt() }
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
                log("Attempt $attempt: installing application $application")
                adbDevice.installPackage(application, true)
                log("Attempt $attempt: application $application installed")
            },
            attemptFailedHandler = { attempt, throwable ->
                log("Attempt $attempt: failed to install application because of ${throwable.message}")
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
            timeoutMinutes = action.timeoutMinutes
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
                                result = TestCaseRun.Result.Failed(
                                    stacktrace = it.message
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
                                result = TestCaseRun.Result.Failed(
                                    stacktrace = """
                                        ${it.message}
                                        ${it.throwable.getStackTraceString()}
                                    """.trimIndent()
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
                log("Attempt $attempt: getting boot_completed param")
                val devicesBooted: Boolean = loadProperty(
                    key = "sys.boot_completed",
                    cast = { output -> output == "1" }
                )
                log("Attempt $attempt: boot_completed param is $devicesBooted")

                if (!devicesBooted) {
                    throw RuntimeException(
                        "Device: $id hasn't booted yet"
                    )
                }

                Device.DeviceStatus.Alive
            },
            attemptFailedHandler = { attempt, throwable ->
                log("Attempt $attempt: device is freezing. Reason: ${throwable.message}")
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
                    command = listOf(
                        "pm", "clear", name
                    )
                )
                log("Attempt: $attempt: clear package $name completed")
            },
            attemptFailedHandler = { attempt, throwable ->
                log("Attempt $attempt: failed to clear package. Reason: ${throwable.message}")
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
                    throw RuntimeException(
                        "Failed to pull file from ${from.toAbsolutePath()} to ${to.toAbsolutePath()}. " +
                            "Result file: ${resultFile.absolutePath} not found."
                    )
                }
            },
            attemptFailedHandler = { attempt, throwable ->
                log("Attempt $attempt: failed to pull $from. Reason: ${throwable.message}")
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
            attemptFailedHandler = { attempt, throwable ->
                log("Attempt $attempt: failed to clear package $remotePath. Reason: ${throwable.message}")
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
            attemptFailedHandler = { attempt, throwable ->
                log("Attempt $attempt: failed to list directory $remotePath. Reason: ${throwable.message}")
            }
        )
    }

    private fun runTest(
        test: TestCase,
        testPackageName: String,
        testRunnerClass: String,
        instrumentationArguments: Map<String, String>,
        outputDir: File,
        timeoutMinutes: Long
    ): Single<InstrumentationTestCaseRun> {
        val logsDir = File(File(outputDir, "logs"), id).apply { mkdirs() }
        val started = System.currentTimeMillis()

        val output = executeShellCommand(
            command = listOf(
                "am",
                "instrument",
                "-w", // wait for instrumentation to finish before returning.  Required for test runners.
                "-r", // raw mode is necessary for parsing
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
                        result = TestCaseRun.Result.Failed(
                            "Timeout"
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

        val bridge = AndroidDebugBridge.createBridge(adb, false)
        waitForAdb(bridge)

        return bridge.devices.find {
            it.serialNumber == id
        } ?: throw RuntimeException("Device $id not found")
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
        .doOnError { throw RuntimeException("Failed to execute command: $command on device $id") }
        .timeout(
            timeoutSeconds,
            TimeUnit.SECONDS,
            Observable.error(
                RuntimeException("Timeout to execute command: $command on device $id")
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
            command = adb,
            args = listOf("-s", id) + command,
            output = redirectOutputTo
        )

    override fun log(message: String) {
        logger.log("[$id $api] $message")
    }

    override fun notifyError(message: String, error: Throwable) {
        logger.notify("[$id $api] $message", error)
    }
}

private const val DEFAULT_COMMAND_TIMEOUT_SECONDS = 5L
private const val DDMLIB_SOCKET_TIME_OUT_SECONDS = 20L
private const val WAIT_FOR_ADB_TIME_OUT_MINUTES = 1L
