package com.avito.android.plugin.build_metrics.internal.jvm

import com.avito.android.Result
import com.avito.android.gradle.profile.BuildProfile
import com.avito.android.plugin.build_metrics.internal.BuildResultListener
import com.avito.android.plugin.build_metrics.internal.BuildStatus
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Timer
import java.util.TimerTask

internal class JvmMetricsListener(
    private val collector: JvmMetricsCollector,
    private val sender: JvmMetricsSender,
    private val period: Duration = Duration.ofSeconds(30)
) : BuildResultListener {

    private val log = LoggerFactory.getLogger(JvmMetricsListener::class.java)
    private val timer: Timer = Timer("jvm-metrics-collector")

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
        val collectResults = collector.collect()

        if (collectResults.isFailure()) {
            log.warn("Disable collecting JVM metrics due to a failure", (collectResults as Result.Failure).throwable)
            cancel()
        }
        collectResults.getOrThrow().forEach { (vm, heapInfo) ->
            sender.send(vm, heapInfo)
        }
    }

    override fun onBuildFinished(status: BuildStatus, profile: BuildProfile) {
        cancel()
    }

    private fun cancel() = timer.cancel()
}
