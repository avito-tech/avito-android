package com.avito.android.plugin.build_metrics.internal.runtime

import com.avito.android.Result
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.plugin.build_metrics.internal.BuildResultListener
import com.avito.android.plugin.build_metrics.internal.BuildStatus
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Timer
import java.util.TimerTask

internal class RuntimeMetricsListener(
    private val collectors: List<MetricsCollector>,
    private val period: Duration = Duration.ofSeconds(30)
) : BuildResultListener {

    override val name: String = "RuntimeMetrics"
    private val log = LoggerFactory.getLogger(RuntimeMetricsListener::class.java)
    private val timer: Timer = Timer("metrics-collector")

    init {
        startCollecting()
    }

    private fun startCollecting() {
        val timerTask = object : TimerTask() {
            override fun run() {
                collect()
            }
        }
        timer.schedule(timerTask, 0, period.toMillis())
    }

    private fun collect() {
        collectors.forEach { collector ->
            val result = collector.collect()

            if (result.isFailure()) {
                log.warn("Disable collecting runtime metrics due to a failure", (result as Result.Failure).throwable)
                cancel()
            }
        }
    }

    override fun onBuildFinished(status: BuildStatus, profile: BuildProfile) {
        cancel()
    }

    private fun cancel() = timer.cancel()
}
