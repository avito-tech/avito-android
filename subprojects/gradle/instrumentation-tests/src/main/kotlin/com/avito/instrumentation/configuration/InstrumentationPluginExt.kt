package com.avito.instrumentation.configuration

import com.avito.android.withAndroidModule
import com.avito.git.gitState
import com.avito.report.model.RunId
import com.avito.report.model.Team
import com.avito.slack.model.SlackChannel
import com.avito.utils.gradle.envArgs
import com.avito.utils.logging.ciLogger
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

internal fun Project.createInstrumentationPluginExtension() {
    val extension =
        extensions.create<InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration>(
            "instrumentation",
            this
        )
    afterEvaluate {
        extension.validate()
    }
}

/**
 * Нужна промежуточная модель т.к светить везде gradle api не очень хорошо.
 * Лишние методы (для dsl) загрязняют скоуп и, что более важное, могут аффектить
 * поведение. Например, наличие динамической конфигурации с помощью NamedDomainObjectContainer
 * мешает нормально сериализации объекта для дальнейшей передачи в воркеры.
 */
internal fun Project.withInstrumentationExtensionData(action: (InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration.Data) -> Unit) {
    withAndroidModule { androidBaseExtension ->
        afterEvaluate {
            val extension = extensions.getByType<InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration>()
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
                .applyParameters(androidBaseExtension.defaultConfig.testInstrumentationRunnerArguments)
                .applyParameters(runIdOverride)
                .applyParameters(
                    mapOf(
                        "reportApiUrl" to extension.reportApiUrl,
                        "reportApiFallbackUrl" to extension.reportApiFallbackUrl,
                        "reportViewerUrl" to extension.reportViewerUrl,
                        "sentryDsn" to extension.sentryDsn,
                        "slackToken" to extension.slackToken,
                        "fileStorageUrl" to extension.fileStorageUrl
                    )
                )
                .applyParameters(extension.instrumentationParams)
                .applyParameters(
                    mapOf(
                        // info for InHouseRunner testRunEnvironment creation
                        "inHouse" to "true"
                    )
                )

            action(
                InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration.Data(
                    configurations = extension.configurations.map { instrumentationConfiguration ->
                        instrumentationConfiguration.data(
                            parentInstrumentationParameters = instrumentationParameters,
                            filters = extension.filters.map { it.toData() }
                        )
                    },
                    pluginInstrumentationParameters = instrumentationParameters,
                    logcatTags = extension.logcatTags,
                    output = extension.output,
                    reportApiUrl = extension.reportApiUrl,
                    reportApiFallbackUrl = extension.reportApiFallbackUrl,
                    reportViewerUrl = extension.reportViewerUrl,
                    fileStorageUrl = extension.fileStorageUrl,
                    registry = extension.registry,
                    slackToken = extension.slackToken,
                    unitToChannelMapping = extension.unitToChannelMap
                        .map { (k, v) -> Team(k) to SlackChannel(v) }
                        .toMap()
                )
            )
        }

    }
}
