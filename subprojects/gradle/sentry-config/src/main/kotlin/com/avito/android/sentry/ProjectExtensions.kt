@file:Suppress("UnstableApiUsage")

package com.avito.android.sentry

import com.avito.git.Git
import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.lazy
import com.avito.kotlin.dsl.lazyProperty
import io.sentry.SentryClient
import org.gradle.api.Project
import org.gradle.api.provider.Provider

fun Project.environmentInfo(): Provider<EnvironmentInfo> = lazyProperty("ENVIRONMENT_INFO_PROVIDER") { project ->
    project.providers.lazy {
        val git = Git.Impl(project.rootDir, project.logger::info)
        EnvironmentInfoImpl(project, git)
    }
}

val Project.sentry: Provider<SentryClient> by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    project.providers.lazy {
        val config = from(project)
        sentryClient(config)
    }
}

val Project.sentryConfig: Provider<SentryConfig> by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    project.providers.lazy {
        from(project)
    }
}

private fun from(project: Project): SentryConfig {
    return if (project.getBooleanProperty("avito.sentry.enabled")) {
        val info = project.environmentInfo().get()
        val tags = mutableMapOf<String, String>()
        tags["ide"] = info.isInvokedFromIde().toString()
        info.teamcityBuildId()?.also { id ->
            tags["build_id"] = id
        }
        SentryConfig.Enabled(
            dsn = project.getMandatoryStringProperty("avito.sentry.dsn"),
            environment = info.environment.publicName,
            serverName = info.node ?: "unknown",
            release = info.commit ?: "unknown",
            tags = tags
        )
    } else {
        SentryConfig.Disabled
    }
}
