package com.avito.logger.destination

import com.avito.logger.LogLevel
import com.avito.logger.LoggingDestination

data class VerboseMode(
    val verbosity: LogLevel,
    val printStackTrace: Boolean
)

/**
 * See Logging.md#Verbose-mode
 */
class VerboseDestination(private val verboseMode: VerboseMode) : LoggingDestination {

    private val verbosity = verboseMode.verbosity

    override fun write(level: LogLevel, message: String, throwable: Throwable?) {
        when (level) {
            LogLevel.DEBUG -> if (verbosity == LogLevel.DEBUG) {
                print(message, throwable)
            }
            LogLevel.INFO -> if (verbosity == LogLevel.DEBUG || verbosity == LogLevel.INFO) {
                print(message, throwable)
            }
            LogLevel.WARNING -> if (verbosity == LogLevel.DEBUG
                || verbosity == LogLevel.INFO
                || verbosity == LogLevel.WARNING
            ) {
                print(message, throwable)
            }
            LogLevel.CRITICAL -> print(message, throwable)
        }
    }

    private fun print(message: String, throwable: Throwable?) {
        println(message)
        if (verboseMode.printStackTrace) {
            throwable?.printStackTrace()
        }
    }
}
