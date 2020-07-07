package com.avito.instrumentation.configuration

import com.avito.report.model.Team
import com.avito.slack.model.SlackChannel
import com.google.common.annotations.VisibleForTesting
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Incubating
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import java.io.Serializable

object InstrumentationPluginConfiguration {
    abstract class GradleInstrumentationPluginConfiguration(
        project: Project
    ) {

        var applicationApk: String? = null
        var testApplicationApk: String? = null

        var reportApiUrl: String = ""
        var reportApiFallbackUrl: String = ""
        var reportViewerUrl: String = ""
        var fileStorageUrl = ""

        var registry: String = ""

        // todo make optional
        var sentryDsn: String = ""

        // todo extract
        var slackToken: String = ""

        var unitToChannelMap: Map<String, String> = emptyMap()

        var applicationProguardMapping: String? = null
        var testProguardMapping: String? = null

        abstract val configurationsContainer: NamedDomainObjectContainer<InstrumentationConfiguration>
        abstract val filters: NamedDomainObjectContainer<InstrumentationFilter>

        @get:Incubating
        val testReport = InstrumentationTestReportExtension()

        val configurations: List<InstrumentationConfiguration>
            get() = configurationsContainer.toList()

        var instrumentationParams: Map<String, String> = emptyMap()

        // https://developer.android.com/studio/command-line/logcat#filteringOutput
        var logcatTags: Collection<String> = emptyList()

        var output: String =
            project.rootProject.file("outputs/${project.name}/instrumentation").path

        fun configurations(closure: Closure<NamedDomainObjectSet<InstrumentationConfiguration>>) {
            configurationsContainer.configure(closure)
        }

        fun filters(action: Action<NamedDomainObjectContainer<InstrumentationFilter>>) {
            action.execute(filters)
        }

        fun testReport(action: Action<InstrumentationTestReportExtension>) {
            action.execute(testReport)
        }

        private fun validate() {
            configurations.forEach {
                it.validate()
            }
            require(sentryDsn.isNotEmpty()) {
                "sentryDsn must be initialized"
            }
            require(slackToken.isNotEmpty()) {
                "slackToken must be initialized"
            }
            require(registry.isNotEmpty()) {
                "registry must be initialized"
            }
        }

        private fun createReportViewer(): Data.ReportViewer? {
            val reportViewer = testReport.reportViewer
            return if (reportViewer != null) {
                Data.ReportViewer(
                    reportApiUrl = reportViewer.reportApiUrl,
                    reportApiFallbackUrl = reportViewer.reportApiFallbackUrl,
                    reportViewerUrl = reportViewer.reportViewerUrl,
                    fileStorageUrl = reportViewer.fileStorageUrl
                )
            } else {
                if (reportApiFallbackUrl.isNotEmpty() && reportViewerUrl.isNotEmpty() && reportApiUrl.isNotEmpty() && fileStorageUrl.isNotEmpty()) {
                    Data.ReportViewer(
                        reportApiUrl = reportApiUrl,
                        reportApiFallbackUrl = reportApiFallbackUrl,
                        reportViewerUrl = reportViewerUrl,
                        fileStorageUrl = fileStorageUrl
                    )
                } else {
                    null
                }
            }
        }

        internal fun toData(params: InstrumentationParameters): Data {
            validate()
            val reportViewer = createReportViewer()
            val instrumentationParameters = params
                .applyParameters(
                    mapOf(
                        "sentryDsn" to sentryDsn,
                        "slackToken" to slackToken,
                        "reportApiUrl" to (reportViewer?.reportApiUrl ?: "http://stub"),
                        "reportApiFallbackUrl" to (reportViewer?.reportApiFallbackUrl ?: "http://stub"),
                        "fileStorageUrl" to (reportViewer?.fileStorageUrl ?: "http://stub"),
                        "reportViewerUrl" to (reportViewer?.reportViewerUrl ?: "http://stub")
                    )
                )
                .applyParameters(instrumentationParams)
                .applyParameters(
                    mapOf(
                        // info for InHouseRunner testRunEnvironment creation
                        "inHouse" to "true"
                    )
                )
            return Data(
                configurations = configurations.map { instrumentationConfiguration ->
                    instrumentationConfiguration.data(
                        parentInstrumentationParameters = instrumentationParameters,
                        filters = filters.map { it.toData() }
                    )
                },
                pluginInstrumentationParameters = instrumentationParameters,
                logcatTags = logcatTags,
                output = output,
                applicationApk = applicationApk,
                testApplicationApk = testApplicationApk,
                reportViewer = reportViewer,
                registry = registry,
                slackToken = slackToken,
                unitToChannelMapping = unitToChannelMap
                    .map { (k, v) -> Team(k) to SlackChannel(v) }
                    .toMap(),
                applicationProguardMapping = applicationProguardMapping,
                testProguardMapping = testProguardMapping
            )
        }

        data class Data(
            val configurations: Collection<InstrumentationConfiguration.Data>,
            private val pluginInstrumentationParameters: InstrumentationParameters,
            val logcatTags: Collection<String>,
            val output: String,
            val applicationApk: String?,
            val testApplicationApk: String?,
            val reportViewer: ReportViewer?,
            val registry: String,
            val slackToken: String,
            val unitToChannelMapping: Map<Team, SlackChannel>,
            val applicationProguardMapping: String?,
            val testProguardMapping: String?
        ) : Serializable {

            data class ReportViewer(
                val reportApiUrl: String,
                val reportApiFallbackUrl: String,
                val reportViewerUrl: String,
                val fileStorageUrl: String
            ) : Serializable

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
}
