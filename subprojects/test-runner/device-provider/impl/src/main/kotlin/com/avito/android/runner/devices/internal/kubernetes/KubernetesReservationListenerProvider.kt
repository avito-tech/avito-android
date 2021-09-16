package com.avito.android.runner.devices.internal.kubernetes

import com.avito.android.stats.StatsDSender
import com.avito.logger.LoggerFactory
import com.avito.runner.service.worker.device.adb.listener.RunnerMetricsConfig
import com.avito.time.TimeProvider

public class KubernetesReservationListenerProvider(
    private val timeProvider: TimeProvider,
    private val runnerMetricsConfig: RunnerMetricsConfig,
    private val loggerFactory: LoggerFactory
) {
    internal fun provide(): KubernetesReservationListener {
        return StatsDKubernetesReservationMetricsSender(
            StatsDSender.create(runnerMetricsConfig.statsDConfig, loggerFactory),
            KubernetesReservationState(timeProvider),
            runnerMetricsConfig.runnerPrefix
        )
    }
}
