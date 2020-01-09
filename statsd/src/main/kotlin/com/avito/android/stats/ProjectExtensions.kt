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
    val enabled = project.getBooleanProperty("avito.stats.enabled", false)

    val logger = project.ciLogger
    val host = project.getMandatoryStringProperty("avito.stats.host")
    val fallbackHost = project.getMandatoryStringProperty("avito.stats.fallbackHost")
    val port = project.getMandatoryIntProperty("avito.stats.port")
    val namespace = project.getMandatoryStringProperty("avito.stats.namespace")

    Providers.of<StatsDSender>(StatsDSender.Impl(
        suppress = !enabled,
        host = host,
        fallbackHost = fallbackHost,
        port = port,
        namespace = namespace,
        logger = { message, error -> if (error != null) logger.info(message, error) else logger.debug(message) }
    ))
}
