package com.avito.runner.service.worker.device

import com.avito.android.Result
import com.avito.logger.Logger
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunAction
import com.avito.runner.service.worker.device.adb.PullValidator
import com.avito.runner.service.worker.model.DeviceInstallation
import java.io.File
import java.nio.file.Path

public interface Device {

    public sealed class Signal {
        public data class Died(val coordinate: DeviceCoordinate) : Signal()
        public data class ReservationNotNeeded(val deviceName: String) : Signal()
        public data class NewDeployment(val deploymentName: String, val deviceName: String) : Signal()
    }

    public val coordinate: DeviceCoordinate

    public val online: Boolean

    public val model: String

    public val api: Int

    public val logger: Logger

    public fun installApplication(applicationPackage: String): Result<DeviceInstallation>

    public suspend fun runIsolatedTest(
        action: InstrumentationTestRunAction,
        outputDir: File
    ): DeviceTestCaseRun

    public fun clearPackage(name: String): Result<Unit>

    /**
     * @return `to` path
     *
     * todo deprecate in favor of [pullDir]
     */
    public fun pull(from: Path, to: Path): Result<File>

    /**
     * @return `hostDir` path
     */
    public fun pullDir(deviceDir: Path, hostDir: Path, validator: PullValidator): Result<File>

    public fun clearDirectory(remotePath: Path): Result<Unit>

    public fun list(remotePath: String): Result<List<String>>

    public fun deviceStatus(): DeviceStatus

    /**
     * Fetch logcat [lines] deep, or whole buffer if null
     */
    public fun logcat(lines: Int?): Result<String>

    public sealed class DeviceStatus {

        public object Alive : DeviceStatus() {
            override fun toString(): String = "alive"
        }

        public class Freeze(public val reason: Throwable) :
            DeviceStatus() {
            override fun toString(): String = "Freeze. Reason: ${reason.message}"
        }
    }
}
