package com.avito.runner.service.worker.device

import com.avito.android.Result
import com.avito.logger.Logger
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunAction
import com.avito.runner.service.worker.model.DeviceInstallation
import java.io.File
import java.nio.file.Path

interface Device {

    sealed class Signal {
        data class Died(val coordinate: DeviceCoordinate) : Signal()
    }

    val coordinate: DeviceCoordinate

    val online: Boolean

    val model: String

    val api: Int

    val logger: Logger

    fun installApplication(applicationPackage: String): Result<DeviceInstallation>

    fun runIsolatedTest(
        action: InstrumentationTestRunAction,
        outputDir: File
    ): DeviceTestCaseRun

    fun clearPackage(name: String): Result<Unit>

    fun pull(from: Path, to: Path): Result<Unit>

    fun clearDirectory(remotePath: Path): Result<Unit>

    fun list(remotePath: String): Result<List<String>>

    fun deviceStatus(): DeviceStatus

    sealed class DeviceStatus {

        object Alive : DeviceStatus() {
            override fun toString(): String = "alive"
        }

        class Freeze(val reason: Throwable) :
            DeviceStatus() {
            override fun toString(): String = "Freeze. Reason: ${reason.message}"
        }
    }
}
