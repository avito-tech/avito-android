package com.avito.instrumentation.reservation.adb

import com.avito.instrumentation.util.waitForCondition
import com.avito.utils.runCommand
import com.avito.utils.spawnProcess
import org.funktionale.tries.Try
import java.io.File

class Device(
    private val serial: String,
    private val logger: (String) -> Unit = {}
) {
    private val androidHome: String? = System.getenv("ANDROID_HOME")
    private val adb: String = "$androidHome/platform-tools/adb"

    init {
        requireNotNull(androidHome) {
            "Can't find env ANDROID_HOME. It needs to run 'adb'"
        }
    }
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

    suspend fun waitForBoot() = waitForCommand(
        runner = { connectAndCheck() },
        checker = { it.exists { output -> output == "1" } },
        successMessage = "$serial connected",
        errorMessage = "failed to connect to $serial"
    )

    fun disconnect(): Try<String> = runCommand(
        command = "$adb disconnect $serial",
        logger = logger
    )

    fun connect(): Try<String> {
        disconnect()

        return runCommand(
            "$adb connect $serial",
            logger = logger
        )
    }

    private suspend fun waitForCommand(
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

    private fun connectAndCheck(): Try<String> {
        connect()
        return executeCommand(CHECK_BOOT_COMPLETED_COMMAND)
    }

    private fun executeCommand(command: String): Try<String> = runCommand(
        command = "$adb -s $serial $command",
        logger = logger
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
