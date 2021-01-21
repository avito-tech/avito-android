package com.avito.runner.scheduler.metrics

import com.avito.runner.service.listener.TestListener

interface TestMetricsListener : TestListener {

    fun onTestSuiteStarted()

    fun onTestSuiteFinished()
}
