@file:Suppress("UnstableApiUsage", "MoveVariableDeclarationIntoWhen")

package com.avito.git

import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.lazyProperty
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property

@JvmOverloads // for using default parameters in build.gradle
fun Project.gitState(logger: (String) -> Unit = {}): Provider<GitState> =
    lazyProperty("GIT_STATE_PROVIDER") { project ->
        project.objects.property<GitState>().apply {
            val strategy = project.getOptionalStringProperty("avito.git.state", default = "env")
            set(
                when (strategy) {
                    "local" -> GitLocalStateImpl.from(project)
                    "env" -> GitStateFromEnvironment.from(project, logger)
                    else -> throw RuntimeException("Unknown git state strategy: $strategy")
                }
            )
        }
    }
