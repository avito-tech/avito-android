package com.avito.teamcity.builds

import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import com.avito.teamcity.TeamcityApi
import org.jetbrains.teamcity.rest.Build
import org.jetbrains.teamcity.rest.BuildConfigurationId
import org.jetbrains.teamcity.rest.Project
import java.time.Instant
import java.time.temporal.ChronoUnit

internal interface TeamcityBuildsProvider {
    fun provide(metricsSourceBuildType: String): Sequence<Build>

    companion object {
        private const val sinceMetricsSendingTimeParameterName = "previousMetricsSendingTime"

        fun create(
            api: TeamcityApi,
            loggerFactory: LoggerFactory,
        ): TeamcityBuildsProvider {
            return Impl(api, loggerFactory)
        }
    }

    private class Impl(
        private val api: TeamcityApi,
        loggerFactory: LoggerFactory,
    ) : TeamcityBuildsProvider {

        private val logger: Logger = loggerFactory.create("TeamcityBuildsProvider")

        override fun provide(metricsSourceBuildType: String): Sequence<Build> {
            logger.info("Provide builds for buildType $metricsSourceBuildType")
            val project = api.getProjectByBuildConfiguration(BuildConfigurationId(metricsSourceBuildType))
            val since = project.getPreviousSendingTime()
            val until = Instant.now()
            project.saveSendingTime(until)
            logger.info("Provide builds end since $since until $until")
            val ranInLast3Hours = getBuild(
                metricsSourceBuildType,
                since.minus(3, ChronoUnit.HOURS),
                until
            )
            val endAtLastHour = ranInLast3Hours.filter { build ->
                val finishTime = requireNotNull(build.finishDateTime) {
                    "Can't be null. Because we don't fetch running builds"
                }
                finishTime.toInstant().isAfter(since)
            }

            logger.info("Found ${endAtLastHour.count()} builds")
            return endAtLastHour
        }

        private fun getBuild(
            metricsSourceBuildType: String,
            since: Instant,
            until: Instant
        ) = api.getBuilds(metricsSourceBuildType) {
            withAllBranches()
                .includeFailed()
                .includeCanceled()
                .since(since)
                .until(until)
        }

        private fun Project.saveSendingTime(time: Instant) {
            setParameter(sinceMetricsSendingTimeParameterName, time.toString())
        }

        private fun Project.getPreviousSendingTime(): Instant {
            val previousSendingTime = parameters.find { it.name == sinceMetricsSendingTimeParameterName }
            require(previousSendingTime != null && previousSendingTime.value.isNotBlank()) {
                """
                |Setup $id. Add $sinceMetricsSendingTimeParameterName configuration parameter.
                |Current value: ${previousSendingTime?.value}
                |Example: ${Instant.now()}""".trimMargin()
            }
            return Instant.parse(previousSendingTime.value)
        }
    }
}
