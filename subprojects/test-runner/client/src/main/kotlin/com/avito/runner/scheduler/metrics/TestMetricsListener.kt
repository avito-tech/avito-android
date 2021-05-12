package com.avito.runner.scheduler.metrics

import com.avito.runner.service.worker.listener.DeviceListener

interface TestMetricsListener : DeviceListener {

    fun onTestSuiteStarted()

    fun onTestSuiteFinished()
}
