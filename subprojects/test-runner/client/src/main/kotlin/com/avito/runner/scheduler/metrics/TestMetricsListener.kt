package com.avito.runner.scheduler.metrics

internal interface TestMetricsListener {

    fun onTestSuiteStarted()

    fun onTestSuiteFinished()
}
