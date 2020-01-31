@file:Suppress("UnstableApiUsage", "MoveVariableDeclarationIntoWhen")

package com.avito.git

import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.lazy
import com.avito.kotlin.dsl.lazyProperty
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider

@JvmOverloads // for using default parameters in build.gradle
fun Project.gitState(logger: (String) -> Unit = {}): Provider<GitState> = lazyProperty("GIT_STATE_PROVIDER") { project ->
    if (project.buildEnvironment is BuildEnvironment.CI) {
        project.providers.lazy {
            val strategy = project.getOptionalStringProperty("avito.git.state", default = "env")
            when (strategy) {
                "local" -> GitLocalStateImpl.from(project)
                "env" -> GitStateFromEnvironment.from(project, logger)
                else -> throw RuntimeException("Unknown git state strategy: $strategy")
            }
        }
    } else {
        logger("build environment not CI, git state not defined")
        Providers.notDefined()
    }
}
