package com.avito.android.runner.devices.internal

import com.avito.android.Result
import com.avito.instrumentation.internal.reservation.adb.waitForCondition
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.worker.device.adb.Adb
import com.avito.utils.ProcessRunner
import java.io.File
import java.time.Duration

internal abstract class AbstractDevice(
    protected val loggerFactory: LoggerFactory,
    protected val processRunner: ProcessRunner,
) : Device {

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

        // TODO stop endless process
        processRunner.spawn(
            command = "$adb -s $serial logcat$tagsString",
            outputTo = file
        )
    }

    protected fun isBootCompleted() =
        processRunner.run(
            command = CHECK_BOOT_COMPLETED_COMMAND,
            timeout = Duration.ofSeconds(10)
        )

    protected suspend fun <T> waitForCommand(
        runner: suspend () -> Result<T>,
    ) = waitForCondition(
        logger = logger,
        conditionName = "Wait device with serial: $serial",
        maxAttempts = 50,
        condition = runner
    )
}

private const val CHECK_BOOT_COMPLETED_COMMAND = "shell getprop sys.boot_completed"
