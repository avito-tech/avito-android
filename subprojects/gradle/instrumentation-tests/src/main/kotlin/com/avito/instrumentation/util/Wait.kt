package com.avito.instrumentation.util

import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

suspend fun waitForCondition(
    logger: (String) -> Unit = {},
    conditionName: String,
    successMessage: String? = null,
    errorMessage: String? = null,
    maxAttempts: Int = WAIT_FOR_COMMAND_MAX_ATTEMPTS,
    frequencySeconds: Long = WAIT_FOR_COMMAND_FREQUENCY_SECONDS,
    condition: suspend () -> Boolean
): Boolean {
    @Suppress("NAME_SHADOWING")
    val successMessage = successMessage ?: "Condition $conditionName succeeded"
    @Suppress("NAME_SHADOWING")
    val errorMessage = errorMessage ?: "Condition $conditionName failed"

    (0..maxAttempts).forEach { attempt ->
        logger("Attempt #$attempt for condition: $conditionName")

        val conditionResult = condition()
        if (conditionResult) {
            logger(successMessage)
            return true
        }

        delay(
            TimeUnit.SECONDS.toMillis(frequencySeconds)
        )
    }
    logger(errorMessage)
    return false
}

private const val WAIT_FOR_COMMAND_MAX_ATTEMPTS = 30
private const val WAIT_FOR_COMMAND_FREQUENCY_SECONDS = 2L
