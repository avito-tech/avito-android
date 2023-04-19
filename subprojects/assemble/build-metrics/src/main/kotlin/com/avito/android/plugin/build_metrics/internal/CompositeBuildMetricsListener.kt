package com.avito.android.plugin.build_metrics.internal

import com.avito.android.gradle.metric.AbstractBuildEventsListener
import com.avito.android.gradle.profile.BuildProfile
import com.avito.logger.LoggerFactory
import org.gradle.BuildResult
import java.time.Instant

internal class CompositeBuildMetricsListener(
    private val listeners: List<BuildResultListener>,
    loggerFactory: LoggerFactory,
) : AbstractBuildEventsListener() {

    override val name: String = "CompositeBuildMetrics"
    private val logger = loggerFactory.create(name)

    override fun buildFinished(buildResult: BuildResult, profile: BuildProfile) {
        if (!isRealBuild(buildResult)) return

        val status = if (buildResult.failure == null) BuildStatus.Success else BuildStatus.Fail

        logger.info("Start build finished ${Instant.now()}")
        listeners.forEach { listener ->
            logger.info("${listener.name} Start build finished ${Instant.now()}")
            listener.onBuildFinished(status, profile)
            logger.info("${listener.name} End build finished ${Instant.now()}")
        }
        logger.info("End build finished ${Instant.now()}")
    }

    private fun isRealBuild(buildResult: BuildResult): Boolean {
        if (!buildResult.isBuildAction()) return false

        if (buildResult.gradle?.startParameter?.isDryRun == true) {
            return false
        }
        return true
    }

    private fun BuildResult.isBuildAction(): Boolean {
        return action == "Build"
    }
}
