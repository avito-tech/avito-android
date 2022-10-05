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
import java.util.logging.Logger
import kotlin.time.ExperimentalTime

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
@ExperimentalTime
@ExperimentalCli
internal class StartWorkerCommand(
    name: String,
    description: String
) : Subcommand(name, description) {

    private val logger = Logger.getLogger("StartWorkerCommand")

    private val configPath: String by option(
        type = ArgType.String,
        fullName = "config",
        shortName = "c",
        description = "Absolute path to worker config"
    ).required()

    private val debugMode: Boolean by option(
        type = ArgType.Boolean,
        fullName = "debug",
        shortName = "d",
        description = "Enables verbose logging",
    ).default(false)

    override fun execute() {
        val reader = ConfigReader(Moshi.Builder().build())
        val config: Config = reader.read(File(configPath))
        logger.info("Apply the config: \n $config")
        val di = WorkerDI(config, debugMode)
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
                    logger.info("Http server stopped")
                    throw it
                }
                .collect { result ->
                    println(result)
                }
        }
    }
}
