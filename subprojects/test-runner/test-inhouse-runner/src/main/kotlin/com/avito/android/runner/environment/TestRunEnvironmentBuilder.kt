package com.avito.android.runner.environment

import com.avito.android.elastic.ElasticConfig
import com.avito.android.runner.args.ArgProvider
import com.avito.android.stats.StatsDConfig
import com.avito.android.test.report.ArgsProvider
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.video.VideoFeatureValue
import com.avito.android.transport.ReportDestination

public interface TestRunEnvironmentBuilder {
    public fun elastic(argProvider: ArgProvider<ElasticConfig>): TestRunEnvironmentBuilder
    public fun statsD(argProvider: ArgProvider<StatsDConfig>): TestRunEnvironmentBuilder
    public fun testMetadata(argProvider: ArgProvider<TestMetadata>): TestRunEnvironmentBuilder
    public fun reportDestination(argProvider: ArgProvider<ReportDestination>): TestRunEnvironmentBuilder
    public fun videoRecording(argProvider: ArgProvider<VideoFeatureValue>): TestRunEnvironmentBuilder
    public fun build(argumentsProvider: ArgsProvider): TestRunEnvironment
}
