package com.avito.teamcity.model

import kotlinx.serialization.Serializable

@Serializable
internal data class TeamcityMetricsSource(
    val configurationId: String,
    val fetchIntervalInHours: Long,
    val metricsPrefix: String,
    val sla: SlaConfig? = null,
) {
    @Serializable
    internal data class SlaConfig(
        /**
         * SLA threshold for the build duration (start → finish), in minutes.
         * Builds longer than the threshold are counted as `bad`; a build that lasts exactly the threshold is good.
         */
        val thresholdMinutes: Long,
        /**
         * Graphite prefix for SLA counters, e.g. `apps.mobile.statistic.sla.android.speed`.
         * The counters are sent as `<metricsPrefix>.total` and `<metricsPrefix>.bad` once per pass.
         */
        val metricsPrefix: String,
    ) {
        init {
            require(thresholdMinutes > 0) { "sla.thresholdMinutes must be positive, got $thresholdMinutes" }
            require(metricsPrefix.isNotBlank()) { "sla.metricsPrefix must not be blank" }
        }
    }
}
