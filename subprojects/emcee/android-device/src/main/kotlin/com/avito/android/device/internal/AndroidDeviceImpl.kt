package com.avito.android.device.internal

import com.avito.android.Result
import com.avito.android.device.AndroidApplication
import com.avito.android.device.AndroidDevice
import com.avito.android.device.DeviceSerial
import com.avito.android.device.InstrumentationCommand
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.request.pkg.UninstallRemotePackageRequest
import com.malinskiy.adam.request.testrunner.InstrumentOptions
import com.malinskiy.adam.request.testrunner.TestAssumptionFailed
import com.malinskiy.adam.request.testrunner.TestEvent
import com.malinskiy.adam.request.testrunner.TestFailed
import com.malinskiy.adam.request.testrunner.TestRunEnded
import com.malinskiy.adam.request.testrunner.TestRunnerRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import java.util.logging.Logger
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class AndroidDeviceImpl(
    override val sdk: Int,
    override val type: String,
    override val serial: DeviceSerial,
    private val installApplicationCommand: InstallApplicationCommand,
    private val adb: AndroidDebugBridgeClient,
) : AndroidDevice {

    private val logger = Logger.getLogger("AndroidDevice")

    override suspend fun install(application: AndroidApplication): Result<Unit> {
        return execute(
            action = {
                runBlocking { installApplicationCommand.installApplicationToDevice(application, serial) }
            },
            errorMessageBuilder = { "Failed to install $application" }
        )
    }

    override suspend fun isAlive(): Boolean {
        val value = "hello"
        val result = adb.execute(
            EchoRequest(value),
            serial = serial.value
        )
        // Output can contain a '\n' character at the end
        return result.exitCode == 0 && result.output.trimIndent() == value
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun executeInstrumentation(
        command: InstrumentationCommand
    ): Result<AndroidDevice.InstrumentationResult> {
        return Result.tryCatch {
            coroutineScope {
                val request = TestRunnerRequest(
                    testPackage = command.testPackage,
                    runnerClass = command.runnerClass,
                    instrumentOptions = InstrumentOptions(
                        clazz = listOf(command.testName)
                    )
                )
                logger.fine("Executing adb shell command: ${String(request.serialize())}")

                val instrumentationOutput = adb.execute(
                    request = request,
                    scope = this,
                    serial = serial.value
                )

                val results = mutableListOf<TestEvent>()
                while (!instrumentationOutput.isClosedForReceive && isActive) {
                    // Add timeout for instrumentation command
                    results.addAll(instrumentationOutput.receiveCatching().getOrElse { emptyList() })
                }
                logger.fine("Instrumentation command output results: $results")
                /**
                 * Parsing is copy-pasted from com.malinskiy.marathon.android.adam.AndroidDeviceTestRunner
                 * It doesn't work perfectly, e.g. result is false-positive when test app does not contain
                 * androidx.test.runner.AndroidJUnitRunner and instrumentation fails with ClassNotFoundException.
                 */
                AndroidDevice.InstrumentationResult(
                    success = results.none { testEvent ->
                        testEvent is TestFailed || testEvent is TestAssumptionFailed
                    } && results.any { testEvent -> testEvent is TestRunEnded }
                )
            }
        }.rescue { error ->
            Result.Failure(RuntimeException("Failed to execute am instrument", error))
        }
    }

    override suspend fun clearPackage(appPackage: String): Result<Unit> =
        execute(
            action = {
                val (output, exitCode) = adb.execute(
                    request = ClearPackageRequest(appPackage = appPackage),
                    serial = serial.value
                )
                if (exitCode != 0) {
                    throw RuntimeException("pm clear $appPackage has exitCode $exitCode. \n\t Output: $output")
                }
            },
            errorMessageBuilder = { "Failed to clear package $appPackage" }
        )

    override suspend fun uninstall(app: AndroidApplication): Result<Unit> =
        execute(
            action = {
                val (output, exitCode) = adb.execute(
                    request = UninstallRemotePackageRequest(
                        packageName = app.packageName,
                        keepData = false
                    ),
                    serial = serial.value
                )
                if (exitCode != 0) {
                    throw RuntimeException(
                        "Uninstall of the ${app.packageName} exitCode $exitCode. \n\t Output: $output"
                    )
                }
            },
            errorMessageBuilder = { "Failed to uninstall ${app.packageName}" }
        )

    private inline fun <T> execute(
        action: () -> T,
        errorMessageBuilder: () -> String
    ): Result<T> {
        return Result
            .tryCatch(action)
            .rescue { error -> Result.Failure(RuntimeException(errorMessageBuilder(), error)) }
    }

    override fun toString(): String {
        return "AndroidDevice(type=$type, sdkVersion=$sdk, serial=$serial)"
    }
}
