package com.avito.android.runner

import androidx.core.content.ContextCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.runner.annotation.resolver.HostAnnotationResolver
import com.avito.android.runner.annotation.resolver.NETWORKING_TYPE_KEY
import com.avito.android.runner.annotation.resolver.NetworkingType
import com.avito.android.runner.annotation.resolver.TEST_METADATA_KEY
import com.avito.android.test.report.ArgsProvider
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.video.VideoFeatureValue
import okhttp3.HttpUrl
import java.io.File
import java.util.UUID

sealed class TestRunEnvironment {

    internal enum class Environment {
        ORCHESTRATOR, IN_HOUSE;

        companion object {
            // https://github.com/android/android-test/blob/2a7336d7a5776eed865011c5e61a055c05d68974/runner/android_test_orchestrator/java/androidx/test/orchestrator/AndroidTestOrchestrator.java#L133
            private val ORCHESTRATOR_KEY = "orchestratorService"
            private val IN_HOUSE_KEY = "inHouse"

            fun getEnvironment(argumentsProvider: ArgsProvider): Environment {
                val isOrchestratorValuePresent = argumentsProvider.getOptionalArgument(ORCHESTRATOR_KEY) != null
                val isInHouseValuePresent = argumentsProvider.getOptionalArgument(IN_HOUSE_KEY) != null
                return when {
                    isOrchestratorValuePresent && isInHouseValuePresent -> throw IllegalStateException("Must be only $ORCHESTRATOR_KEY or $IN_HOUSE_KEY")
                    !isOrchestratorValuePresent && !isInHouseValuePresent -> throw IllegalStateException("Must be on of keys [$ORCHESTRATOR_KEY, $IN_HOUSE_KEY]")
                    isOrchestratorValuePresent -> ORCHESTRATOR
                    isInHouseValuePresent -> IN_HOUSE
                    else -> throw IllegalStateException("I have no idea what i'm doing")
                }
            }
        }
    }

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

    /**
     * We use TestOrchestrator when run tests from Android Studio, for consistency with CI runs (isolated app processes)
     * Orchestrator runs this runner before any test to determine which tests to run and spawn separate processes
     *
     * If it is this special run, we don't need to run any of our special moves here, better skip all of them
     *
     * @link https://developer.android.com/training/testing/junit-runner#using-android-test-orchestrator
     */
    object OrchestratorFakeRunEnvironment : TestRunEnvironment()

    data class RunEnvironment(
        val isImitation: Boolean,

        val deviceId: String,
        val planSlug: String,
        val jobSlug: String,
        val deviceName: String,
        val runId: String,
        val teamcityBuildId: Int,

        val buildBranch: String,
        val buildCommit: String,
        val testMetadata: TestMetadata,
        val networkType: NetworkingType,
        /**
         * Основное место где определяется apiUrl для тестов
         * Будет использоваться как для всех запросов приложения, так и для всех сервисов ресурсов (RM, messenger, integration...)
         *
         * TestApplication подхватит значение из бандла, который там доступен в статике,
         * putString здесь как раз переопредяет его чтобы оно не оказалось пустым
         *
         * Ресурсы инциализируются в графе даггера и получают этот инстанс HttpUrl
         *
         * 1. Аннотация имеет главный приоритет см. [com.avito.android.runner.annotation.resolver.AnnotationResolver] и [HostAnnotationResolver] в частности
         * 2. далее instrumentation аргумент [apiUrlParameterKey]
         */
        val apiUrl: HttpUrl,
        val mockWebServerUrl: String,
        val slackToken: String,
        val videoRecordingFeature: VideoFeatureValue,
        val outputDirectory: Lazy<File>,
        val sentryDsn: String,
        val fileStorageUrl: String,
        val reportApiUrl: String,
        val reportApiFallbackUrl: String,
        val reportViewerUrl: String
    ) : TestRunEnvironment()
}

fun provideEnvironment(
    apiUrlParameterKey: String,
    mockWebServerUrl: String,
    argumentsProvider: ArgsProvider
): TestRunEnvironment {
    val environment = TestRunEnvironment.Environment.getEnvironment(argumentsProvider)
    return when (environment) {
        TestRunEnvironment.Environment.ORCHESTRATOR -> TestRunEnvironment.OrchestratorFakeRunEnvironment
        TestRunEnvironment.Environment.IN_HOUSE -> TestRunEnvironment.RunEnvironment(
            isImitation = argumentsProvider.getOptionalArgument("imitation") == "true",
            deviceId = argumentsProvider.getOptionalArgument("deviceId")
                ?: UUID.randomUUID().toString(),
            planSlug = argumentsProvider.getMandatoryArgument("planSlug"),
            jobSlug = argumentsProvider.getMandatoryArgument("jobSlug"),
            deviceName = argumentsProvider.getMandatoryArgument("deviceName"),
            runId = argumentsProvider.getMandatoryArgument("runId"),
            teamcityBuildId = argumentsProvider.getMandatoryArgument("teamcityBuildId").toInt(),
            buildBranch = argumentsProvider.getMandatoryArgument("buildBranch"),
            buildCommit = argumentsProvider.getMandatoryArgument("buildCommit"),
            testMetadata = argumentsProvider.getMandatorySerializableArgument(TEST_METADATA_KEY) as TestMetadata,
            networkType = argumentsProvider.getMandatorySerializableArgument(NETWORKING_TYPE_KEY) as NetworkingType,
            //todo url'ы не обязательные параметры
            apiUrl = provideApiUrl(
                argumentsProvider = argumentsProvider,
                apiUrlParameterKey = apiUrlParameterKey
            ),
            mockWebServerUrl = mockWebServerUrl,
            videoRecordingFeature = provideVideoRecordingFeature(
                argumentsProvider = argumentsProvider
            ),
            outputDirectory = lazy {
                ContextCompat.getExternalFilesDirs(
                    InstrumentationRegistry.getInstrumentation().targetContext,
                    null
                )[0]
            },
            // from [com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration]
            slackToken = argumentsProvider.getMandatoryArgument("slackToken"),
            sentryDsn = argumentsProvider.getMandatoryArgument("sentryDsn"),
            fileStorageUrl = argumentsProvider.getMandatoryArgument("fileStorageUrl"),
            reportApiUrl = argumentsProvider.getMandatoryArgument("reportApiUrl"),
            reportApiFallbackUrl = argumentsProvider.getMandatoryArgument("reportApiFallbackUrl"),
            reportViewerUrl = argumentsProvider.getMandatoryArgument("reportViewerUrl")
        )
    }
}

private fun provideApiUrl(
    argumentsProvider: ArgsProvider,
    apiUrlParameterKey: String
): HttpUrl {
    val host = (argumentsProvider.getOptionalArgument(HostAnnotationResolver.KEY)
        ?: argumentsProvider.getOptionalArgument(apiUrlParameterKey)
        ?: error("Instrumentation argument $apiUrlParameterKey is required"))

    return HttpUrl.get(host)
}

private fun provideVideoRecordingFeature(argumentsProvider: ArgsProvider): VideoFeatureValue {
    val videoRecordingArgument = argumentsProvider.getOptionalArgument("videoRecording")

    return when (argumentsProvider.getOptionalArgument("videoRecording")) {
        null, "disabled" -> VideoFeatureValue.Disabled
        "failed" -> VideoFeatureValue.Enabled.OnlyFailed
        "all" -> VideoFeatureValue.Enabled.All
        else -> throw IllegalArgumentException(
            "Failed to resolve video recording resolution from argument: $videoRecordingArgument"
        )
    }
}
