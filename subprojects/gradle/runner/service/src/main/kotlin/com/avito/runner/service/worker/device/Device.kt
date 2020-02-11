package com.avito.runner.service.worker.device

import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.DeviceTestCaseRun
import com.avito.runner.service.model.intention.InstrumentationTestRunAction
import com.avito.runner.service.worker.model.DeviceInstallation
import org.funktionale.tries.Try
import java.io.File
import java.nio.file.Path

interface Device {
    val id: String
    val online: Boolean
    val api: Int

    fun installApplication(application: String): DeviceInstallation

    fun runIsolatedTest(
        action: InstrumentationTestRunAction,
        outputDir: File,
        listener: TestListener?
    ): DeviceTestCaseRun

    fun clearPackage(name: String): Try<Any>

    fun pull(from: Path, to: Path): Try<Any>
    fun clearDirectory(remotePath: Path): Try<Any>
    fun list(remotePath: String): Try<Any>

    fun deviceStatus(): DeviceStatus

    fun log(message: String)
    fun notifyError(message: String, error: Throwable)

    sealed class DeviceStatus {
        object Alive : DeviceStatus() {
            override fun toString(): String = "alive"
        }

        class Freeze(val reason: Throwable) :
            DeviceStatus() {
            override fun toString(): String = "freeze because of: ${reason.message}"
        }
    }
}
