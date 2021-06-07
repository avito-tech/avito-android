package com.avito.runner.scheduler.metrics

interface TestMetricsListener {

    fun onTestSuiteStarted()

    fun onTestSuiteFinished()
}
