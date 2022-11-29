package com.avito.emcee.worker

import com.avito.emcee.worker.internal.config.ConfigReader
import com.avito.emcee.worker.internal.di.WorkerDI
import com.squareup.moshi.Moshi
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.cli.required
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.SimpleFormatter
import kotlin.time.ExperimentalTime

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
@ExperimentalTime
@ExperimentalCli
internal class StartWorkerCommand(
    name: String,
    description: String,
) : Subcommand(name, description) {

    private val logger = Logger.getLogger("StartWorkerCommand")

    private val configPath: String by option(
        type = ArgType.String,
        fullName = "config",
        shortName = "c",
        description = "Absolute path to worker config"
    ).required()

    private val logLevel: String by option(
        type = ArgType.String,
        fullName = "logLevel",
        shortName = "ll",
        description = "Log level",
    ).default("info")

    override fun execute() {
        setupLoggingLevel()

        val reader = ConfigReader(Moshi.Builder().build())
        val config: Config = reader.read(File(configPath))
        logger.info("Apply the config: $config")
        val di = WorkerDI(config)
        val httpServer = di.httpServer()
        val producer = di.producer()
        val consumer = di.consumer()
        runBlocking {
            httpServer.start()
            val jobs = producer.getJobs()
            consumer
                .consume(jobs)
                .catch {
                    httpServer.stop()
                    throw it
                }
                .collect { result ->
                    logger.info("Test finished with result: $result")
                }
        }
    }

    private fun setupLoggingLevel() {
        val targetLevel: Level = when (this.logLevel.lowercase(Locale.getDefault())) {
            "all" -> Level.ALL
            "config" -> Level.CONFIG
            "fine" -> Level.FINE
            "finer" -> Level.FINER
            "finest" -> Level.FINEST
            "info" -> Level.INFO
            "off" -> Level.OFF
            "severe" -> Level.SEVERE
            "warning" -> Level.WARNING
            else -> error("Undefined log level passed: ${this.logLevel}")
        }
        Logger.getLogger("").apply {
            level = targetLevel
            for (handler in handlers) {
                handler.level = targetLevel

                // Sets up logging format similar to Emcee queue
                handler.formatter = object : SimpleFormatter() {

                    // 2022-11-28 16:05:30.500
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS", Locale.ROOT)

                    override fun format(record: LogRecord): String {
                        // [INFO] 2022-11-25 16:05:30.500: Message
                        return "[${record.level}] ${dateFormatter.format(Date(record.millis))}: ${record.message}\n"
                    }
                }
            }
        }
    }
}
