@file:Suppress("UnstableApiUsage")

package com.avito.android.graphite

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.utils.logging.ciLogger
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider

val Project.graphite: Provider<GraphiteSender> by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    val logger = project.ciLogger

    Providers.of<GraphiteSender>(GraphiteSender.Impl(
        config = config(project),
        logger = { message, error -> if (error != null) logger.info(message, error) else logger.debug(message) }
    ))
}

val Project.graphiteConfig: Provider<GraphiteConfig> by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    Providers.of(config(project))
}

private fun config(project: Project): GraphiteConfig =
    GraphiteConfig(
        isEnabled = project.getBooleanProperty("avito.graphite.enabled", false),
        host = project.getMandatoryStringProperty("avito.graphite.host"),
        port = project.getMandatoryIntProperty("avito.graphite.port"),
        namespace = project.getMandatoryStringProperty("avito.graphite.namespace")
    )
