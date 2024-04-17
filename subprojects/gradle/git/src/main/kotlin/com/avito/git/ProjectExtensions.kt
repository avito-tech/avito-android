@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.avito.git

import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.lazyProperty
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property

/**
 * Warning! used in build.gradle files
 */
@Deprecated(
    "Break configuration cache",
    replaceWith = ReplaceWith("gitStateProvider()", "com.avito.git.gitStateProvider")
)
public fun Project.gitState(): Property<GitState> =
    lazyProperty("GIT_STATE_PROVIDER_DEPRECATED") { project ->
        project.objects.property<GitState>().apply {
            val strategy = project.getOptionalStringProperty("avito.git.state", default = "local")
            set(
                when (strategy) {
                    "local" -> GitLocalState.from(
                        project = project
                    )

                    "env" -> GitStateFromEnvironment.from(
                        project = project
                    )

                    else -> throw RuntimeException("Unknown git state strategy: $strategy")
                }
            )
        }
    }

public fun Project.gitStateProvider(): Provider<GitState> =
    project.providers.of(GitStateValueSource::class.java) {
        it.parameters.strategy.set(project.getOptionalStringProperty("avito.git.state", default = "local"))
        it.parameters.gitBranch.set(project.getOptionalStringProperty("gitBranch"))
        it.parameters.targetBranch.set(project.getOptionalStringProperty("targetBranch"))
        it.parameters.originalCommitHash.set(project.getOptionalStringProperty("originalCommitHash"))
    }
