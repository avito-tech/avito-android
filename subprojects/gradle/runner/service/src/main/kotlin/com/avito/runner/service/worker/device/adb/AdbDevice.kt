package com.avito.runner.service.worker.device.adb

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.DdmPreferences
import com.android.ddmlib.IDevice
import com.avito.android.stats.StatsDSender
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
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
import com.avito.runner.service.worker.device.adb.listener.AdbDeviceEventsListener
import com.avito.runner.service.worker.device.adb.listener.AdbDeviceEventsLogger
import com.avito.runner.service.worker.device.adb.listener.AdbDeviceMetrics
import com.avito.runner.service.worker.device.adb.listener.CompositeAdbDeviceEventListener
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.runner.service.worker.device.model.getData
import com.avito.runner.service.worker.model.DeviceInstallation
import com.avito.runner.service.worker.model.Installation
import com.avito.runner.service.worker.model.InstrumentationTestCaseRun
import com.avito.time.TimeProvider
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
    private val timeProvider: TimeProvider,
    private val loggerFactory: LoggerFactory,
    private val metricsConfig: RunnerMetricsConfig? = null,
    // MBS-8531: don't use "ADB" here to avoid possible recursion
    override val logger: Logger = loggerFactory.create("[${coordinate.serial}]"),
    private val eventsListener: AdbDeviceEventsListener = createEventListener(
        loggerFactory = loggerFactory,
        logger = logger,
        runnerMetricsConfig = metricsConfig
    ),
    private val commandLine: CommandLineExecutor = CommandLineExecutor.Impl(),
    private val instrumentationParser: InstrumentationTestCaseRunParser = InstrumentationTestCaseRunParser.Impl()
) : Device {

    override val api: Int by lazy {
        retry(
            retriesCount = 5,
            delaySeconds = 3,
            block = { attempt ->
                val result = loadProperty(
                    key = "ro.build.version.sdk",
                    cast = { it.toInt() }
                )
                eventsListener.onGetSdkPropertySuccess(attempt, result)
                result
            },
            attemptFailedHandler = { attempt, _ ->
                eventsListener.onGetSdkPropertyAttemptFail(attempt)
            },
            actionFailedHandler = { throwable ->
                eventsListener.onGetSdkPropertyFailure(throwable)
            }
        )
    }

    override fun installApplication(applicationPackage: String): Try<DeviceInstallation> {
        var installStartedTimestamp = 0L
        return getAdbDevice().flatMap { adbDevice ->

            installStartedTimestamp = timeProvider.nowInMillis()

            runWithRetries(
                retriesCount = 10,
                delaySeconds = 5,
                block = { attempt ->
                    adbDevice.installPackage(applicationPackage, true)
                    eventsListener.onInstallApplicationSuccess(this, attempt, applicationPackage)
                },
                attemptFailedHandler = { attempt, _ ->
                    eventsListener.onInstallApplicationAttemptFail(this, attempt, applicationPackage)
                },
                onError = { throwable ->
                    eventsListener.onInstallApplicationFailure(this, applicationPackage, throwable)
                }
            )
        }.map {
            DeviceInstallation(
                installation = Installation(
                    application = applicationPackage,
                    timestampStartedMilliseconds = installStartedTimestamp,
                    timestampCompletedMilliseconds = timeProvider.nowInMillis()
                ),
                device = this.getData()
            )
        }
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
                        val testName = "${it.className}.${it.name}"
                        when (it.result) {
                            TestCaseRun.Result.Passed -> eventsListener.onRunTestPassed(this, testName)
                            TestCaseRun.Result.Ignored -> eventsListener.onRunTestIgnored(this, testName)
                            is TestCaseRun.Result.Failed.InRun ->
                                eventsListener.onRunTestRunError(
                                    device = this,
                                    testName = testName,
                                    errorMessage = it.result.errorMessage
                                )
                            is TestCaseRun.Result.Failed.InfrastructureError ->
                                eventsListener.onRunTestInfrastructureError(
                                    device = this,
                                    testName = testName,
                                    errorMessage = it.result.errorMessage,
                                    throwable = it.result.cause
                                )
                        }
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
                        eventsListener.onRunTestFailedOnStart(this, it.message)
                        DeviceTestCaseRun(
                            testCaseRun = TestCaseRun(
                                test = action.test,
                                result = TestCaseRun.Result.Failed.InfrastructureError(
                                    errorMessage = "Failed on start test case: ${it.message}"
                                ),
                                timestampStartedMilliseconds = timeProvider.nowInMillis(),
                                timestampCompletedMilliseconds = timeProvider.nowInMillis()
                            ),
                            device = this.getData()
                        )
                    }
                    is InstrumentationTestCaseRun.FailedOnInstrumentationParsing -> {
                        eventsListener.onRunTestFailedOnInstrumentationParse(this, it.message, it.throwable)
                        DeviceTestCaseRun(
                            testCaseRun = TestCaseRun(
                                test = action.test,
                                result = TestCaseRun.Result.Failed.InfrastructureError(
                                    errorMessage = "Failed on instrumentation parsing: ${it.message}",
                                    cause = it.throwable
                                ),
                                timestampStartedMilliseconds = timeProvider.nowInMillis(),
                                timestampCompletedMilliseconds = timeProvider.nowInMillis()
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
                val bootCompleted: Boolean = loadProperty(
                    key = "sys.boot_completed",
                    cast = { output -> output == "1" }
                )

                if (!bootCompleted) {
                    throw IllegalStateException("sys.boot_completed isn't '1'")
                }

                eventsListener.onGetAliveDeviceSuccess(this, attempt)

                Device.DeviceStatus.Alive
            },
            attemptFailedHandler = { attempt, _ ->
                eventsListener.onGetAliveDeviceAttemptFail(this, attempt)
            },
            actionFailedHandler = { throwable ->
                eventsListener.onGetAliveDeviceFailed(this, throwable)
            }
        )
    } catch (t: Throwable) {
        Device.DeviceStatus.Freeze(reason = t)
    }

    override fun clearPackage(name: String): Try<Any> = Try {
        runWithRetries(
            retriesCount = 10,
            delaySeconds = 2,
            block = { attempt ->
                val result = executeBlockingShellCommand(
                    command = listOf("pm", "clear", name)
                )

                if (result.output != "Success") {
                    throw IllegalStateException("Fail to clear package $name; output=${result.output}")
                }

                eventsListener.onClearPackageSuccess(this, attempt, name)
            },
            attemptFailedHandler = { attempt, throwable ->
                eventsListener.onClearPackageAttemptFail(this, attempt, name, throwable)
            },
            onError = { throwable ->
                eventsListener.onClearPackageFailure(this, name, throwable)
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

                eventsListener.onPullSuccess(this, from, to)
            },
            attemptFailedHandler = { attempt, throwable ->
                eventsListener.onPullAttemptFail(this, attempt, from, throwable)
            },
            actionFailedHandler = { throwable ->
                eventsListener.onPullFailure(this, from, throwable)
            }
        )
    }

    override fun clearDirectory(remotePath: Path): Try<Any> = Try {
        retry(
            retriesCount = 5,
            delaySeconds = 3,
            block = {
                val result = executeBlockingShellCommand(
                    command = listOf(
                        "rm",
                        "-rfv",
                        remotePath.toString()
                    )
                )

                if (result.output.contains("removed ")) {
                    eventsListener.onClearDirectorySuccess(this, remotePath, result.output)
                } else {
                    eventsListener.onClearDirectoryNothingDone(this, remotePath)
                }
            },
            attemptFailedHandler = { attempt, throwable ->
                eventsListener.onClearDirectoryAttemptFail(this, attempt, remotePath, throwable)
            },
            actionFailedHandler = { throwable ->
                eventsListener.onClearDirectoryFailure(this, remotePath, throwable)
            }
        )
    }

    override fun list(remotePath: String): Try<List<String>> = Try {
        retry(
            retriesCount = 5,
            delaySeconds = 3,
            block = {
                val result = executeBlockingShellCommand(
                    command = listOf(
                        "ls",
                        remotePath
                    )
                ).output.lines()

                eventsListener.onListSuccess(this, remotePath)

                result
            },
            attemptFailedHandler = { attempt, throwable ->
                eventsListener.onListAttemptFail(this, attempt, remotePath, throwable)
            },
            actionFailedHandler = { throwable ->
                eventsListener.onListFailure(this, remotePath, throwable)
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
        val started = timeProvider.nowInMillis()

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

    private fun getAdbDevice(): Try<IDevice> = Try {
        AndroidDebugBridge.initIfNeeded(false)
        DdmPreferences.setTimeOut(Duration.ofSeconds(DDMLIB_SOCKET_TIME_OUT_SECONDS).toMillis().toInt())

        val bridge = AndroidDebugBridge.createBridge(adb.adbPath, false)
        waitForAdb(bridge)

        bridge.devices.find { it.serialNumber == coordinate.serial.value }
            ?: throw RuntimeException("Device $coordinate not found")
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
                RuntimeException(
                    "Timeout: $timeoutSeconds seconds. Failed to execute command: $command on device $coordinate"
                )
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

    private fun <T> runWithRetries(
        retriesCount: Int,
        delaySeconds: Long = 1,
        attemptFailedHandler: (attempt: Int, throwable: Throwable) -> Unit = { _, _ -> },
        onError: (throwable: Throwable) -> Unit = {},
        block: (attempt: Int) -> T
    ): Try<T> {
        for (attempt in 0..retriesCount) {
            if (attempt > 0) TimeUnit.SECONDS.sleep(delaySeconds)
            try {
                return Try.Success(block(attempt))
            } catch (e: Throwable) {
                if (attempt == retriesCount - 1) {
                    onError(e)
                    return Try.Failure(e)
                } else {
                    attemptFailedHandler(attempt, e)
                }
            }
        }
        throw IllegalStateException("retry must return value or throw exception")
    }

    override fun toString(): String = "Device ${coordinate.serial}"
}

private const val DEFAULT_COMMAND_TIMEOUT_SECONDS = 5L
private const val DDMLIB_SOCKET_TIME_OUT_SECONDS = 20L
private const val WAIT_FOR_ADB_TIME_OUT_MINUTES = 1L

private fun createEventListener(
    loggerFactory: LoggerFactory,
    logger: Logger,
    runnerMetricsConfig: RunnerMetricsConfig?
): AdbDeviceEventsListener {
    return if (runnerMetricsConfig == null) {
        AdbDeviceEventsLogger(logger)
    } else {
        CompositeAdbDeviceEventListener(
            listOf(
                AdbDeviceEventsLogger(logger),
                AdbDeviceMetrics(
                    statsDSender = StatsDSender.Impl(
                        config = runnerMetricsConfig.statsDConfig,
                        loggerFactory = loggerFactory
                    ),
                    runnerPrefix = runnerMetricsConfig.runnerPrefix
                )
            )
        )
    }
}
