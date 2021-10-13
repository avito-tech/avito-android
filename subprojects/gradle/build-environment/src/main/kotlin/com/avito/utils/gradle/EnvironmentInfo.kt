package com.avito.android.sentry

import com.avito.git.Git
import com.avito.kotlin.dsl.lazyProperty
import com.avito.utils.gradle.Environment
import com.avito.utils.gradle.internal.EnvironmentInfoImpl
import org.gradle.api.Project
import org.gradle.api.provider.Provider

/**
 * Use [Project.environmentInfo] to gain instance
 */
public interface EnvironmentInfo { // TODO: merge with BuildEnvironment and EnvArgs
    public val node: String?
    public val environment: Environment
    public fun teamcityBuildId(): String?
}

public fun Project.environmentInfo(): Provider<EnvironmentInfo> =
    lazyProperty("ENVIRONMENT_INFO_PROVIDER") { project ->
        project.providers.provider {
            val git = Git.create(
                rootDir = project.rootDir,
            )
            EnvironmentInfoImpl(project, lazy { git.config("user.email") })
        }
    }
