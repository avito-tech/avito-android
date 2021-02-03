package com.avito.android.runner.devices.internal

import com.avito.instrumentation.internal.reservation.adb.waitForCondition
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.worker.device.Serial
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.runCommand
import com.avito.utils.spawnProcess
import org.funktionale.tries.Try
import java.io.File

internal abstract class Device(protected val loggerFactory: LoggerFactory) {

    private val logger = loggerFactory.create<Device>()

    protected abstract val adb: Adb

    abstract val serial: Serial

    fun redirectLogcatToFile(
        file: File,
        tags: Collection<String> = emptyList()
    ) {
        val tagsString = if (tags.isEmpty()) {
            ""
        } else {
            " ${tags.joinToString(" ")}"
        }

        executeNonBlockingCommand(
            command = "logcat$tagsString",
            redirectOutputTo = file
        )
    }

    abstract suspend fun waitForBoot(): Boolean

    protected fun isBootCompleted() = executeCommand(CHECK_BOOT_COMPLETED_COMMAND)

    protected suspend fun waitForCommand(
        runner: () -> Try<String>,
        checker: (Try<String>) -> Boolean,
        successMessage: String,
        errorMessage: String
    ) = waitForCondition(
        logger = logger,
        conditionName = "Wait device with serial: $serial",
        successMessage = successMessage,
        errorMessage = errorMessage,
        maxAttempts = 50
    ) {
        checker(
            runner()
        )
    }

    private fun executeCommand(command: String): Try<String> = runCommand(
        command = "$adb -s $serial $command",
        loggerFactory = loggerFactory
    )

    private fun executeNonBlockingCommand(
        command: String,
        redirectOutputTo: File? = null
    ): Process =
        spawnProcess(
            command = "$adb -s $serial $command",
            outputTo = redirectOutputTo
        )
}

private const val CHECK_BOOT_COMPLETED_COMMAND = "shell getprop sys.boot_completed"
