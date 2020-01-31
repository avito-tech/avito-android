package com.avito.android.stats

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.utils.logging.ciLogger
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider

val Project.statsd: Provider<StatsDSender> by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    val logger = project.ciLogger

    Providers.of<StatsDSender>(StatsDSender.Impl(
        config = config(project),
        logger = { message, error -> if (error != null) logger.info(message, error) else logger.debug(message) }
    ))
}

val Project.statsdConfig: Provider<StatsDConfig> by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    Providers.of(config(project))
}

private fun config(project: Project): StatsDConfig = StatsDConfig(
    isEnabled = project.getBooleanProperty("avito.stats.enabled", false),
    host = project.getMandatoryStringProperty("avito.stats.host"),
    fallbackHost = project.getMandatoryStringProperty("avito.stats.fallbackHost"),
    port = project.getMandatoryIntProperty("avito.stats.port"),
    namespace = project.getMandatoryStringProperty("avito.stats.namespace")
)
