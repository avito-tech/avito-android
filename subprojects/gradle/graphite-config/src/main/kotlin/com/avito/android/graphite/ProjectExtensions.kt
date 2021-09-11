package com.avito.android.graphite

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryIntProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider

public val Project.graphiteConfig: Provider<GraphiteConfig> by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    Providers.of(config(project))
}

private fun config(project: Project): GraphiteConfig =
    GraphiteConfig(
        isEnabled = project.getBooleanProperty("avito.graphite.enabled", false),
        debug = project.getBooleanProperty("avito.graphite.debug", false),
        host = project.getMandatoryStringProperty("avito.graphite.host"),
        port = project.getMandatoryIntProperty("avito.graphite.port"),
        namespace = project.getMandatoryStringProperty("avito.graphite.namespace")
    )
