package com.avito.runner.scheduler.metrics.model

import java.time.Duration

internal sealed class TestExecutionEvent {

    fun start(currentTime: Duration): TestExecutionEvent = when (this) {
        is IntentionReceived -> Started(intentionReceived, currentTime)
        is Started -> error("Can't start already started $this")
        is Finished -> error("Can't start already finished $this")
    }

    fun finish(currentTime: Duration): TestExecutionEvent = when (this) {
        is IntentionReceived -> error("Can't finish not started $this")
        is Started -> Finished(intentionReceived, testStarted, currentTime)
        is Finished -> error("Can't finish already finished $this")
    }

    /**
     * Device worker got intention from queue
     */
    data class IntentionReceived(
        val intentionReceived: Duration
    ) : TestExecutionEvent()

    /**
     * Device worker prepared device for test execution
     */
    data class Started(
        val intentionReceived: Duration,
        val testStarted: Duration
    ) : TestExecutionEvent()

    /**
     * Device worker finished test execution successfully
     */
    data class Finished(
        val intentionReceived: Duration,
        val testStarted: Duration,
        val finished: Duration
    ) : TestExecutionEvent() {
        val effectiveWorkTime: Duration = finished - intentionReceived
        val installationTime: Duration = testStarted - intentionReceived
    }
}
