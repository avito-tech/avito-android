package com.avito.android.tech_budget.internal.warnings.log

/**
 * Saves logs to a files.
 *
 * For [LogEntry.taskName] `compileRelease` running in [ProjectInfo.path] `:avito-app:core` it will generate:
 *
 * * avito-app_core/.project - project's meta info (it's path)
 * * avito-app_core/compileRelease.log - logs from task
 */
internal class FileLogWriter(
    private val fileProvider: LogFileProjectProvider,
    private val separator: String = DEFAULT_SEPARATOR,
) : LogWriter {

    override fun save(logMessage: String) {
        fileProvider.provideLogFile().appendText(formatLogMessage(logMessage))
    }

    /**
     * Format: "text of log|||"
     */
    private fun formatLogMessage(message: String): String =
        buildString {
            append(message)
            append(separator)
            appendLine()
        }

    companion object {
        const val DEFAULT_SEPARATOR = "|||"
    }
}
