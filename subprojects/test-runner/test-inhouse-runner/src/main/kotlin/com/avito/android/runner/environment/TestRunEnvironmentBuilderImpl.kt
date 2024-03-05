package com.avito.android.runner.environment

import com.avito.android.elastic.ElasticConfig
import com.avito.android.runner.annotation.resolver.TEST_METADATA_KEY
import com.avito.android.runner.args.ArgProvider
import com.avito.android.runner.args.ReportDestinationArgParser
import com.avito.android.runner.args.VideoFeatureArgParser
import com.avito.android.stats.StatsDConfig
import com.avito.android.test.report.ArgsProvider
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.video.VideoFeatureValue
import com.avito.android.transport.ReportDestination

internal class TestRunEnvironmentBuilderImpl : TestRunEnvironmentBuilder {

    private var elastic: ArgProvider<ElasticConfig>? = null
    private var statsD: ArgProvider<StatsDConfig>? = null
    private var testMetadata: ArgProvider<TestMetadata> =
        ArgProvider { args -> args.getSerializableArgumentOrThrow(TEST_METADATA_KEY) }
    private var reportDestination: ArgProvider<ReportDestination> = ReportDestinationArgParser()
    private var videoRecording: ArgProvider<VideoFeatureValue> = VideoFeatureArgParser()

    override fun elastic(argProvider: ArgProvider<ElasticConfig>): TestRunEnvironmentBuilder {
        this.elastic = argProvider
        return this
    }

    override fun statsD(argProvider: ArgProvider<StatsDConfig>): TestRunEnvironmentBuilder {
        this.statsD = argProvider
        return this
    }

    override fun testMetadata(argProvider: ArgProvider<TestMetadata>): TestRunEnvironmentBuilder {
        this.testMetadata = argProvider
        return this
    }

    override fun reportDestination(argProvider: ArgProvider<ReportDestination>): TestRunEnvironmentBuilder {
        this.reportDestination = argProvider
        return this
    }

    override fun videoRecording(argProvider: ArgProvider<VideoFeatureValue>): TestRunEnvironmentBuilder {
        this.videoRecording = argProvider
        return this
    }

    override fun build(argumentsProvider: ArgsProvider): TestRunEnvironment {
        return try {
            TestRunEnvironment.RunEnvironment(
                testMetadata = testMetadata.parse(argumentsProvider),
                videoRecordingFeature = videoRecording.parse(argumentsProvider),
                reportDestination = reportDestination.parse(argumentsProvider),
                elasticConfig = elastic?.parse(argumentsProvider) ?: ElasticConfig.Disabled,
                statsDConfig = statsD?.parse(argumentsProvider) ?: StatsDConfig.Disabled,
            )
        } catch (e: Throwable) {
            TestRunEnvironment.InitError(e.message ?: "Can't parse arguments for creating TestRunEnvironment")
        }
    }
}
