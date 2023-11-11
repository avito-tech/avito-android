package com.avito.android.runner.environment

import com.avito.android.elastic.ElasticConfig
import com.avito.android.stats.StatsDConfig
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.video.VideoFeatureValue
import com.avito.android.transport.ReportDestination

sealed interface TestRunEnvironment {

    fun asRunEnvironmentOrThrow(): RunEnvironment {
        if (this !is RunEnvironment) {
            throw RuntimeException("Expected run environment type: RunEnvironment, actual: $this")
        }

        return this
    }

    fun executeIfRealRun(action: (RunEnvironment) -> Unit) {
        if (this is RunEnvironment) {
            action(this)
        }
    }

    data class InitError(val error: String) : TestRunEnvironment

    data class RunEnvironment internal constructor(
        val testMetadata: TestMetadata,
        internal val reportDestination: ReportDestination,
        internal val videoRecordingFeature: VideoFeatureValue,
        internal val elasticConfig: ElasticConfig,
        internal val statsDConfig: StatsDConfig,
    ) : TestRunEnvironment
}
