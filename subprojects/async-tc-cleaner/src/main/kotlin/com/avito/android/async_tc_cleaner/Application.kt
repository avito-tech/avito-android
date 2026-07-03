package com.avito.android.async_tc_cleaner

import com.avito.android.async_tc_cleaner.internal.config.Config
import com.avito.android.async_tc_cleaner.internal.config.EnvBasedConfigFactory
import com.avito.android.async_tc_cleaner.internal.observability.CleanerLoggerFactory
import com.avito.android.async_tc_cleaner.internal.observability.NoOpReapObserver
import com.avito.android.async_tc_cleaner.internal.observability.ReapObserver
import com.avito.android.async_tc_cleaner.internal.observability.StatsdMetricsReapObserver
import com.avito.android.async_tc_cleaner.internal.reaper.JvmTreeDeleter
import com.avito.android.async_tc_cleaner.internal.reaper.Reaper
import com.avito.android.async_tc_cleaner.internal.reaper.RmzDeleter
import com.avito.android.async_tc_cleaner.internal.reaper.TreeDeleter
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import kotlin.time.Duration

public fun main(): Unit = runBlocking {
    val config = EnvBasedConfigFactory().create().getOrThrow()
    runCleaner(config, CleanerLoggerFactory.create(config))
}

internal suspend fun runCleaner(config: Config, loggerFactory: LoggerFactory) {
    val logger = loggerFactory.create("async-tc-cleaner")

    val elastic = config.elastic
    if (elastic != null) {
        logger.info("Elastic logging enabled: index=${elastic.index}, endpoints=${elastic.endpoints.size}")
    } else {
        logger.info("Elastic logging disabled (no ELASTIC_ENDPOINTS)")
    }

    val reaper = Reaper(
        config = config,
        observer = createReapObserver(config, loggerFactory, logger),
        logger = logger,
        deleter = createTreeDeleter(config, logger),
    )

    coroutineScope {
        val sweepingJob = launch { reaper.start() }
        registerShutdownHook(sweepingJob, config.shutdownTimeout, logger)
        sweepingJob.join()
    }
}

private fun createTreeDeleter(config: Config, logger: Logger): TreeDeleter {
    val rmzPath = config.rmzPath
    if (File(rmzPath).canExecute()) {
        logger.info("Deleting trees with rmz: rmz=$rmzPath")
        return RmzDeleter(rmzPath = rmzPath)
    }
    logger.info("rmz not found at $rmzPath; deleting trees in-process with the JVM")
    return JvmTreeDeleter()
}

private fun createReapObserver(config: Config, loggerFactory: LoggerFactory, logger: Logger): ReapObserver {
    val statsd = config.statsd
    if (statsd == null) {
        logger.info("StatsD disabled (no STATSD_HOST)")
        return NoOpReapObserver
    }
    logger.info(
        "StatsD enabled: host=${statsd.host}, fallbackHost=${statsd.fallbackHost}, " +
            "port=${statsd.port}, namespace=${statsd.namespace}"
    )
    return StatsdMetricsReapObserver.create(statsd, config.node, loggerFactory)
}

private fun registerShutdownHook(job: Job, timeout: Duration, logger: Logger) {
    Runtime.getRuntime().addShutdownHook(
        Thread {
            runBlocking {
                val stopped = withTimeoutOrNull(timeout) {
                    job.cancelAndJoin()
                    true
                } ?: false
                if (!stopped) {
                    logger.warn("Timed out waiting for cleaner shutdown after $timeout")
                }
            }
        }
    )
}
