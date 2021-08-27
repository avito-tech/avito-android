package com.avito.runner.scheduler.metrics

import com.avito.math.Percent
import java.time.Duration

internal class StubTestMetricsSender : TestRunnerMetricsSender {

    override fun sendInitialDelay(value: Duration) {
        // no op
    }

    override fun sendEndDelay(value: Duration) {
        // no op
    }

    override fun sendMedianQueueTime(value: Duration) {
        // no op
    }

    override fun sendMedianInstallationTime(value: Duration) {
        // no op
    }

    override fun sendSuiteTime(value: Duration) {
        // no op
    }

    override fun sendTotalTime(value: Duration) {
        // no op
    }

    override fun sendMedianDeviceUtilization(percent: Percent) {
        // no op
    }
}
