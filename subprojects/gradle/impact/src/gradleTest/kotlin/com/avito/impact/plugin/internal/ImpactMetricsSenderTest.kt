package com.avito.impact.plugin.internal

import com.avito.android.build_metrics.BuildMetricTracker
import com.avito.android.sentry.StubEnvironmentInfo
import com.avito.android.stats.GaugeLongMetric
import com.avito.android.stats.StatsMetric
import com.avito.android.stats.StubStatsdSender
import com.avito.impact.StubModifiedProjectsFinder
import com.avito.module.configurations.ConfigurationType
import com.avito.test.gradle.androidApp
import com.avito.test.gradle.androidLib
import com.avito.test.gradle.rootProject
import com.avito.utils.gradle.Environment
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("MagicNumber")
internal class ImpactMetricsSenderTest {

    private lateinit var projectsFinder: StubModifiedProjectsFinder
    private lateinit var statsdSender: StubStatsdSender
    private lateinit var sender: ImpactMetricsSender

    @BeforeEach
    fun setup() {
        statsdSender = StubStatsdSender()
        projectsFinder = StubModifiedProjectsFinder()
        val environmentInfo = StubEnvironmentInfo(
            environment = Environment.CI
        )
        val metricsPrefix = BuildMetricTracker(environmentInfo, statsdSender)
        sender = ImpactMetricsSender(projectsFinder, environmentInfo, metricsPrefix)
    }

    @Test
    fun `send - modified modules and apps`() {
        val root = rootProject()
        val notChangedApp = androidApp(":app-not-changed", root)
        val notChangedLib = androidLib(":lib-not-changed", root)

        val changedLib = androidLib(":lib-changed", root)
        val changedApp = androidApp(":app-changed", root)

        projectsFinder.addProjects(notChangedApp, notChangedLib)
        projectsFinder.addModifiedProject(changedLib, ConfigurationType.Main)
        projectsFinder.addModifiedProject(changedApp, ConfigurationType.AndroidTests)

        sender.sendMetrics()

        val metrics = statsdSender.getSentMetrics()

        metrics.find {
            it.name.toString().contains("impact.modules.implementation.modified")
        }.also { metric ->
            verifyGaugeValue(metric, 25)
        }

        metrics.find {
            it.name.toString().contains("impact.modules.androidtests.modified")
        }.also { metric ->
            verifyGaugeValue(metric, 25)
        }

        metrics.find {
            it.name.toString().contains("impact.apps.implementation.modified")
        }.also { metric ->
            verifyGaugeValue(metric, 0)
        }

        metrics.find {
            it.name.toString().contains("impact.apps.androidtests.modified")
        }.also { metric ->
            verifyGaugeValue(metric, 50)
        }
    }

    private fun verifyGaugeValue(metric: StatsMetric?, value: Number) {
        assertThat(metric).isNotNull()
        metric!!

        assertThat(metric).isInstanceOf(GaugeLongMetric::class.java)
        val gaugeMetric = metric as GaugeLongMetric

        assertThat(gaugeMetric.value).isEqualTo(value)
    }
}
