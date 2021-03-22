package com.avito.android.runner.devices.internal

import com.avito.android.Result
import com.avito.instrumentation.internal.reservation.adb.waitForCondition
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.runCommand
import com.avito.utils.spawnProcess
import java.io.File

internal abstract class AbstractDevice(protected val loggerFactory: LoggerFactory) : Device {

    private val logger = loggerFactory.create<AbstractDevice>()

    protected abstract val adb: Adb

    override fun redirectLogcatToFile(
        file: File,
        tags: Collection<String>
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

    protected fun isBootCompleted() = executeCommand(CHECK_BOOT_COMPLETED_COMMAND)

    protected suspend fun <T> waitForCommand(
        runner: suspend () -> Result<T>,
    ) = waitForCondition(
        logger = logger,
        conditionName = "Wait device with serial: $serial",
        maxAttempts = 50,
        condition = runner
    )

    private fun executeCommand(command: String): Result<String> = runCommand(
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
