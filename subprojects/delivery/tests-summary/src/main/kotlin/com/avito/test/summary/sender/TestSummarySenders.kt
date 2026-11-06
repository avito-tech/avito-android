package com.avito.test.summary.sender

import com.avito.android.Result
import com.avito.logger.Logger
import com.avito.test.summary.time.TimeMessenger
import com.avito.test.summary.time.TimeMessengerClient

/**
 * Time only duplicates Alertino summaries,
 * so a broken Time configuration must not fail the task before Alertino sends.
 */
internal fun testSummarySenders(
    alertinoSender: TestSummarySender,
    timeBaseUrl: String?,
    timeToken: String?,
    timeSender: (TimeMessenger) -> TestSummarySender,
    logger: Logger,
): List<TestSummarySender> {
    if (timeToken.isNullOrBlank()) {
        logger.info("Time token is not set, test summary is not sent to Time")
        return listOf(alertinoSender)
    }
    return Result.tryCatch {
        val baseUrl = requireNotNull(timeBaseUrl) { "time.baseUrl is not set" }
        timeSender(TimeMessengerClient(baseUrl = baseUrl, token = timeToken))
    }.fold(
        onSuccess = { listOf(alertinoSender, it) },
        onFailure = { error ->
            logger.warn("Cannot initialize Time sender, continuing with Alertino", error)
            listOf(alertinoSender)
        }
    )
}
