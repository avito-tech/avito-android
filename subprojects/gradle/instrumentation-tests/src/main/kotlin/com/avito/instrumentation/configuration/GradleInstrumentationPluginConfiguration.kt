package com.avito.instrumentation.configuration

import com.avito.report.model.Team
import com.avito.slack.model.SlackChannel
import com.google.common.annotations.VisibleForTesting
import groovy.lang.Closure
import org.gradle.api.Action
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
        var registry: String = ""
        var sentryDsn: String = ""
        var slackToken: String = ""
        var fileStorageUrl = ""

        var unitToChannelMap: Map<String, String> = emptyMap()

        abstract val configurationsContainer: NamedDomainObjectContainer<InstrumentationConfiguration>
        abstract val filters: NamedDomainObjectContainer<InstrumentationFilter>

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

        fun filters(action: Action<NamedDomainObjectSet<InstrumentationFilter>>) {
            action.execute(filters)
        }

        fun validate() {
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

        data class Data(
            val configurations: Collection<InstrumentationConfiguration.Data>,
            private val pluginInstrumentationParameters: InstrumentationParameters,
            val logcatTags: Collection<String>,
            val output: String,
            val applicationApk: String?,
            val testApplicationApk: String?,
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
}
