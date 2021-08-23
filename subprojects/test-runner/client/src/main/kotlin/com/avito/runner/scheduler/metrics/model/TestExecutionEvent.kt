package com.avito.runner.scheduler.metrics.model

import java.time.Duration
import java.time.Instant

internal sealed class TestExecutionEvent {

    fun start(currentTime: Instant): TestExecutionEvent = when (this) {
        is IntentionReceived -> Started(intentionReceived, currentTime)
        is Started -> error("Can't start already started $this")
        is Finished -> error("Can't start already finished $this")
    }

    fun finish(currentTime: Instant): TestExecutionEvent = when (this) {
        is IntentionReceived -> error("Can't finish not started $this")
        is Started -> Finished(intentionReceived, testStarted, currentTime)
        is Finished -> error("Can't finish already finished $this")
    }

    /**
     * Device worker got intention from queue
     */
    data class IntentionReceived(
        val intentionReceived: Instant
    ) : TestExecutionEvent()

    /**
     * Device worker prepared device for test execution
     */
    data class Started(
        val intentionReceived: Instant,
        val testStarted: Instant
    ) : TestExecutionEvent()

    /**
     * Device worker finished test execution successfully
     */
    data class Finished(
        val intentionReceived: Instant,
        val testStarted: Instant,
        val finished: Instant
    ) : TestExecutionEvent() {
        val effectiveWorkTime: Duration = Duration.between(intentionReceived, finished)
        val installationTime: Duration = Duration.between(intentionReceived, testStarted)
    }
}
