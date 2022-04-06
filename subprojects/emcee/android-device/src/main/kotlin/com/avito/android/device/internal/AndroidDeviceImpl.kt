package com.avito.android.device.internal

import com.avito.android.Result
import com.avito.android.device.AndroidApplication
import com.avito.android.device.AndroidDevice
import com.avito.android.device.DeviceSerial
import com.avito.android.device.InstrumentationCommand
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.request.pkg.StreamingPackageInstallRequest
import com.malinskiy.adam.request.pkg.UninstallRemotePackageRequest
import com.malinskiy.adam.request.testrunner.InstrumentOptions
import com.malinskiy.adam.request.testrunner.TestAssumptionFailed
import com.malinskiy.adam.request.testrunner.TestFailed
import com.malinskiy.adam.request.testrunner.TestRunEnded
import com.malinskiy.adam.request.testrunner.TestRunnerRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.coroutineScope

internal class AndroidDeviceImpl(
    override val sdk: Int,
    override val type: String,
    override val serial: DeviceSerial,
    private val adb: AndroidDebugBridgeClient,
) : AndroidDevice {

    override suspend fun install(application: AndroidApplication): Result<Unit> {
        return execute(
            action = {
                val success = adb.execute(
                    StreamingPackageInstallRequest(
                        pkg = application.apk,
                        supportedFeatures = emptyList(),
                        reinstall = false,
                        extraArgs = emptyList()
                    ),
                    serial = serial.value
                )
                if (!success) {
                    throw RuntimeException("Streaming install of the $application not success")
                }
            },
            errorMessageBuilder = { "Failed to install $application" }
        )
    }

    override suspend fun isAlive(): Boolean {
        val result = adb.execute(
            EchoHelloRequest,
            serial = serial.value
        )
        return result.exitCode == 0 && result.output == "hello"
    }

    @OptIn(ExperimentalCoroutinesApi::class, kotlin.time.ExperimentalTime::class)
    override suspend fun executeInstrumentation(
        command: InstrumentationCommand
    ): Result<AndroidDevice.InstrumentationResult> {
        return Result.tryCatch {
            coroutineScope {
                val instrumentationOutput = adb.execute(
                    request = TestRunnerRequest(
                        testPackage = command.testPackage,
                        runnerClass = command.runnerClass,
                        instrumentOptions = InstrumentOptions(
                            clazz = listOf(command.testName)
                        )
                    ),
                    scope = this,
                    serial = serial.value
                )
                val result = instrumentationOutput.receiveOrNull() ?: emptyList()
                // parsing copy-pasting from com.malinskiy.marathon.android.adam.AndroidDeviceTestRunner
                AndroidDevice.InstrumentationResult(
                    success = result.none { testEvent ->
                        testEvent is TestFailed || testEvent is TestAssumptionFailed
                    } && result.any { testEvent -> testEvent is TestRunEnded }
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
}
