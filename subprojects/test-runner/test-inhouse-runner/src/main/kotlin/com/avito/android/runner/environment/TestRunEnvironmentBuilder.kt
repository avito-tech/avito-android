package com.avito.android.runner.environment

import com.avito.android.elastic.ElasticConfig
import com.avito.android.runner.args.ArgProvider
import com.avito.android.stats.StatsDConfig
import com.avito.android.test.report.ArgsProvider
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.video.VideoFeatureValue
import com.avito.android.transport.ReportDestination

interface TestRunEnvironmentBuilder {
    fun elastic(argProvider: ArgProvider<ElasticConfig>): TestRunEnvironmentBuilder
    fun statsD(argProvider: ArgProvider<StatsDConfig>): TestRunEnvironmentBuilder
    fun testMetadata(argProvider: ArgProvider<TestMetadata>): TestRunEnvironmentBuilder
    fun reportDestination(argProvider: ArgProvider<ReportDestination>): TestRunEnvironmentBuilder
    fun videoRecording(argProvider: ArgProvider<VideoFeatureValue>): TestRunEnvironmentBuilder
    fun build(argumentsProvider: ArgsProvider): TestRunEnvironment
}
