package com.avito.instrumentation.configuration

import com.avito.android.withAndroidModule
import com.avito.git.gitState
import com.avito.report.model.RunId
import com.avito.report.model.Team
import com.avito.slack.model.SlackChannel
import com.avito.utils.gradle.envArgs
import com.avito.utils.logging.ciLogger
import com.google.common.annotations.VisibleForTesting
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.create
import java.io.Serializable

class InstrumentationPluginConfiguration internal constructor(
    private val project: Project,
    private val configuration: GradleInstrumentationPluginConfiguration
) : Configuration<InstrumentationPluginConfiguration.Data> {

    constructor(project: Project) : this(
        project,
        project.extensions.create<GradleInstrumentationPluginConfiguration>("instrumentation")
    ) {
        configuration.configurationsContainer = project.container()

        configuration.configurationsContainer.all {
            it.targetsContainer = project.container()
        }

        project.afterEvaluate {
            configuration.validate()
        }
    }

    /**
     * Нужна промежуточная модель т.к светить везде gradle api не очень хорошо.
     * Лишние методы (для dsl) загрязняют скоуп и, что более важное, могут аффектить
     * поведение. Например, наличие динамической конфигурации с помощью NamedDomainObjectContainer
     * мешает нормально сериализации объекта для дальнейшей передачи в воркеры.
     */
    override fun withData(action: (Data) -> Unit) {
        project.withAndroidModule { baseExtension ->
            project.afterEvaluate {
                val env = project.envArgs
                val runId = project.gitState { project.ciLogger.info(it) }
                    .map { gitState ->
                        RunId(
                            commitHash = gitState.currentBranch.commit,
                            buildTypeId = env.buildTypeId
                        )
                    }.orNull

                val runIdOverride = runId?.let {
                    mapOf("runId" to it.toString())
                } ?: emptyMap()

                val instrumentationParameters = InstrumentationParameters()
                    .applyParameters(baseExtension.defaultConfig.testInstrumentationRunnerArguments)
                    .applyParameters(runIdOverride)
                    .applyParameters(
                        mapOf(
                            "reportApiUrl" to configuration.reportApiUrl,
                            "reportApiFallbackUrl" to configuration.reportApiFallbackUrl,
                            "reportViewerUrl" to configuration.reportViewerUrl,
                            "sentryDsn" to configuration.sentryDsn,
                            "slackToken" to configuration.slackToken,
                            "fileStorageUrl" to configuration.fileStorageUrl
                        )
                    )
                    .applyParameters(configuration.instrumentationParams)
                    .applyParameters(
                        mapOf(
                            // info for InHouseRunner testRunEnvironment creation
                            "inHouse" to "true"
                        )
                    )

                action(
                    Data(
                        configurations = configuration.configurations.map { instrumentationConfiguration ->
                            instrumentationConfiguration.data(
                                parentInstrumentationParameters = instrumentationParameters
                            )
                        },
                        pluginInstrumentationParameters = instrumentationParameters,
                        logcatTags = configuration.logcatTags,
                        output = configuration.output,
                        reportApiUrl = configuration.reportApiUrl,
                        reportApiFallbackUrl = configuration.reportApiFallbackUrl,
                        reportViewerUrl = configuration.reportViewerUrl,
                        fileStorageUrl = configuration.fileStorageUrl,
                        registry = configuration.registry,
                        slackToken = configuration.slackToken,
                        unitToChannelMapping = configuration.unitToChannelMap
                            .map { (k, v) -> Team(k) to SlackChannel(v) }
                            .toMap()
                    )
                )
            }
        }
    }

    open class GradleInstrumentationPluginConfiguration {

        var reportApiUrl: String = ""
        var reportApiFallbackUrl: String = ""
        var reportViewerUrl: String = ""
        var registry: String = ""
        var sentryDsn: String = ""
        var slackToken: String = ""
        var fileStorageUrl = ""

        var unitToChannelMap: Map<String, String> = emptyMap()

        lateinit var configurationsContainer: NamedDomainObjectContainer<InstrumentationConfiguration>

        val configurations: List<InstrumentationConfiguration>
            get() = configurationsContainer.toList()

        var instrumentationParams: Map<String, String> = emptyMap()

        // https://developer.android.com/studio/command-line/logcat#filteringOutput
        var logcatTags: Collection<String> = emptyList()

        lateinit var output: String

        fun configurations(closure: Closure<NamedDomainObjectContainer<InstrumentationConfiguration>>) {
            configurationsContainer.configure(closure)
        }

        fun validate() {
            require(::output.isInitialized) {
                "instrumentation.output property must be set"
            }

            require(configurations.isNotEmpty()) { "instrumentation plugin applied without configurations" }
            configurations.forEach {
                it.validate()
            }
            require(reportApiFallbackUrl.isNotEmpty()) {
                "reportApiFallbackUrl must be initialized"
            }
            require(reportViewerUrl.isNotEmpty()) {
                "reportViewerUrl must be initialized"
            }
            require(reportApiUrl.isNotEmpty()) {
                "reportApiUrl must be initialized"
            }
            require(sentryDsn.isNotEmpty()) {
                "sentryDsn must be initialized"
            }
            require(slackToken.isNotEmpty()) {
                "slackToken must be initialized"
            }
            require(fileStorageUrl.isNotEmpty()) {
                "fileStorageUrl must be initialized"
            }
            require(registry.isNotEmpty()) {
                "registry must be initialized"
            }
        }
    }

    data class Data(
        val configurations: Collection<InstrumentationConfiguration.Data>,
        private val pluginInstrumentationParameters: InstrumentationParameters,
        val logcatTags: Collection<String>,
        val output: String,
        val reportApiUrl: String,
        val reportApiFallbackUrl: String,
        val reportViewerUrl: String,
        val fileStorageUrl: String,
        val registry: String,
        val slackToken: String,
        val unitToChannelMapping: Map<Team, SlackChannel>
    ) : Serializable {

        /**
         * Из-за того что раньше instrumentationParameters были публичными, их использовали ошибочно,
         * т.к. там еще не учтены override из конфигураций (например часто это новый jobSlug)
         *
         * возможно в дальшейшем пригодится доступ к параметрам на этом уровне,
         * поэтому поле и тест на этот уровень сохранены
         */
        @VisibleForTesting
        internal fun checkPluginLevelInstrumentationParameters(): InstrumentationParameters {
            return pluginInstrumentationParameters
        }
    }

}
