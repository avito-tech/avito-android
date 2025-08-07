package com.avito.android.plugin.build_metrics.internal.di

import com.avito.android.clickstream.ClickStreamEventTracker
import com.avito.android.clickstream.ClickStreamSenderImpl
import com.avito.android.clickstream.config.ClickStreamConfig
import com.avito.android.graphite.GraphiteConfig
import com.avito.android.graphite.GraphiteSender
import com.avito.android.plugin.build_metrics.BuildEnvironment
import com.avito.android.plugin.build_metrics.internal.core.BuildMetricSender
import com.avito.android.stats.StatsDConfig
import com.avito.android.stats.StatsDSender
import com.avito.android.stats.withPrefix
import com.avito.graphite.series.SeriesName
import com.avito.logger.LoggerFactory

internal class BuildMetricsSenderProvider(
    buildType: String,
    environment: BuildEnvironment,
    statsDConfig: StatsDConfig,
    graphiteConfig: GraphiteConfig,
    clickStreamConfig: ClickStreamConfig,
    isTest: Boolean,
    loggerFactory: LoggerFactory,
) {

    private val pluginMetricsPrefix = SeriesName.create("builds")
        .addTag("build_type", buildType)
        .addTag("env", environment.code)

    private val statsdSender: StatsDSender = StatsDSender.create(
        config = statsDConfig,
        loggerFactory = loggerFactory,
    ).withPrefix(pluginMetricsPrefix)

    private val graphiteSender by lazy {
        val metricPrefix = graphiteConfig
            .metricPrefix
            .append(pluginMetricsPrefix)

        GraphiteSender.create(
            config = graphiteConfig.copy(
                metricPrefix = metricPrefix, ignoreExceptions = true
            ),
            loggerFactory = loggerFactory,
            isTest = isTest,
        )
    }

    private val clickStreamEventTracker by lazy {
        ClickStreamEventTracker(
            clickStreamSender = ClickStreamSenderImpl(
                config = clickStreamConfig,
            ),
        )
    }

    fun provide(): BuildMetricSender {
        return BuildMetricSender.create(statsdSender, graphiteSender, clickStreamEventTracker)
    }
}
