package com.avito.android.runner.environment

import com.avito.android.elastic.ElasticConfig
import com.avito.android.stats.StatsDConfig
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.video.VideoFeatureValue
import com.avito.android.transport.ReportDestination

public sealed interface TestRunEnvironment {

    public fun asRunEnvironmentOrThrow(): RunEnvironment {
        if (this !is RunEnvironment) {
            throw RuntimeException("Expected run environment type: RunEnvironment, actual: $this")
        }

        return this
    }

    public fun executeIfRealRun(action: (RunEnvironment) -> Unit) {
        if (this is RunEnvironment) {
            action(this)
        }
    }

    /**
     * We use TestOrchestrator when run tests from Android Studio, for consistency with CI runs (isolated app processes)
     * Orchestrator runs this runner before any test to determine which tests to run and spawn separate processes
     *
     * If it is this special run, we don't need to run any of our special moves here, better skip all of them
     *
     * @link https://developer.android.com/training/testing/junit-runner#using-android-test-orchestrator
     */
    public object OrchestratorFakeRunEnvironment : TestRunEnvironment {

        override fun toString(): String = this::class.java.simpleName
    }

    public data class InitError(val error: String) : TestRunEnvironment

    public data class RunEnvironment internal constructor(
        val testMetadata: TestMetadata,
        internal val reportDestination: ReportDestination,
        internal val videoRecordingFeature: VideoFeatureValue,
        internal val elasticConfig: ElasticConfig,
        internal val statsDConfig: StatsDConfig,
    ) : TestRunEnvironment
}
