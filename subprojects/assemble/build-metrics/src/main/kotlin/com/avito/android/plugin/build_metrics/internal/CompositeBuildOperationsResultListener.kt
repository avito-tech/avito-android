package com.avito.android.plugin.build_metrics.internal

import com.avito.logger.LoggerFactory
import java.time.Instant

internal class CompositeBuildOperationsResultListener(
    private val listeners: List<BuildOperationsResultListener>,
    loggerFactory: LoggerFactory,
) : BuildOperationsResultListener {

    override val name: String = "CompositeBuildOperations"
    private val logger = loggerFactory.create(name)
    override fun onBuildFinished(result: BuildOperationsResult) {
        logger.info("Start onBuildFinished ${Instant.now()}")
        listeners.forEach {
            logger.info("${it.name} Start onBuildFinished ${Instant.now()}")
            it.onBuildFinished(result)
            logger.info("${it.name} End onBuildFinished ${Instant.now()}")
        }
        logger.info("End onBuildFinished ${Instant.now()}")
    }
}
