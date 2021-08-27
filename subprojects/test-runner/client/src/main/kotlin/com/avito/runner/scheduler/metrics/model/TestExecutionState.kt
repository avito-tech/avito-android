package com.avito.runner.scheduler.metrics.model

import java.time.Duration
import java.time.Instant

internal sealed class TestExecutionState {

    abstract val test: TestKey

    abstract fun start(currentTime: Instant): Started
    abstract fun complete(currentTime: Instant): Completed

    /**
     * Device worker got intention from queue
     */
    data class IntentionReceived(
        val intentionReceived: Instant,
        override val test: TestKey
    ) : TestExecutionState() {

        override fun start(currentTime: Instant) = Started(intentionReceived, currentTime, test)

        override fun complete(currentTime: Instant): Completed {
            throw UnsupportedOperationException("Can't completed IntentionReceived")
        }
    }

    /**
     * Device worker prepared device for test execution
     */
    data class Started(
        val intentionReceived: Instant,
        val testStarted: Instant,
        override val test: TestKey

    ) : TestExecutionState() {

        override fun start(currentTime: Instant): Started {
            throw UnsupportedOperationException("Can't start Started")
        }

        override fun complete(currentTime: Instant) = Completed(intentionReceived, testStarted, currentTime, test)
    }

    /**
     * Device worker completed test execution successfully
     */
    data class Completed(
        val intentionReceived: Instant,
        val testStarted: Instant,
        val completed: Instant,
        override val test: TestKey
    ) : TestExecutionState() {
        val effectiveWorkTime: Duration = Duration.between(intentionReceived, completed)
        val installationTime: Duration = Duration.between(intentionReceived, testStarted)

        override fun start(currentTime: Instant): Started {
            throw UnsupportedOperationException("Can't start Completed")
        }

        override fun complete(currentTime: Instant): Completed {
            throw UnsupportedOperationException("Can't complete Completed")
        }
    }

    companion object
}
