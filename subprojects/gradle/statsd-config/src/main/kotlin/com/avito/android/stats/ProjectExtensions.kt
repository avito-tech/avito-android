package com.avito.android.stats

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.logger.GradleLoggerFactory
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider

val Project.statsd: Provider<StatsDSender> by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    Providers.of(
        StatsDSender.Impl(
            config = config(project),
            loggerFactory = GradleLoggerFactory.fromProject(project)
        )
    )
}

val Project.statsdConfig: Provider<StatsDConfig> by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    Providers.of(config(project))
}

// todo need fail on configuration phase
private fun config(project: Project): StatsDConfig {
    val isEnabled = project.getBooleanProperty("avito.stats.enabled", false)
    return if (isEnabled) {
        StatsDConfig.Enabled(
            host = project.getMandatoryStringProperty("avito.stats.host"),
            fallbackHost = project.getMandatoryStringProperty("avito.stats.fallbackHost"),
            port = project.getMandatoryIntProperty("avito.stats.port"),
            namespace = SeriesName.create(
                project.getMandatoryStringProperty("avito.stats.namespace"),
                multipart = true
            )
        )
    } else {
        StatsDConfig.Disabled
    }
}
