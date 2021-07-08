package com.avito.runner.scheduler.metrics

internal interface TestSuiteListener {

    fun onTestSuiteStarted()

    fun onTestSuiteFinished()
}
