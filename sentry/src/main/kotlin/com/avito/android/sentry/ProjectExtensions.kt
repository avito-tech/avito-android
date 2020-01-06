@file:Suppress("UnstableApiUsage")

package com.avito.android.sentry

import com.avito.git.Git
import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import com.avito.kotlin.dsl.lazy
import com.avito.kotlin.dsl.lazyProperty
import io.sentry.SentryClient
import org.gradle.api.Project
import org.gradle.api.provider.Provider

fun Project.environmentInfo(): Provider<EnvironmentInfo> = lazyProperty("ENVIRONMENT_INFO_PROVIDER") { project ->
    project.providers.lazy {
        val git = Git.Impl(project.rootDir, project.logger::info)
        EnvironmentInfoImpl(project, git) as EnvironmentInfo
    }
}

val Project.sentry: Provider<SentryClient> by ProjectProperty.lazy(scope = ROOT_PROJECT) { project ->
    project.providers.lazy {
        val config = SentryConfig.from(project)
        sentryClient(config)
    }
}
