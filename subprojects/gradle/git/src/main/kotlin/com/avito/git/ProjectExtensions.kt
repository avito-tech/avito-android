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

/**
 * Reads git state at configuration time. The result is tracked by the configuration
 * cache, so any git operation that changes it invalidates CC. Prefer [gitInfoService]
 * and read git state at task-execution time; migration is tracked in MBSA-2353.
 */
public fun Project.gitStateProvider(): Provider<GitState> =
    project.providers.of(GitStateValueSource::class.java) {
        configureGitStateParameters(it.parameters)
    }

/**
 * CC-safe replacement for [gitStateProvider]. See [GitInfoBuildService].
 */
public fun Project.gitInfoService(): Provider<GitInfoBuildService> =
    project.gradle.sharedServices.registerIfAbsent(
        GitInfoBuildService.NAME,
        GitInfoBuildService::class.java,
    ) { spec ->
        configureGitStateParameters(spec.parameters)
    }

private fun Project.configureGitStateParameters(parameters: GitStateParameters) {
    parameters.strategy.set(getOptionalStringProperty("avito.git.state", default = "local"))
    parameters.gitBranch.set(getOptionalStringProperty("gitBranch"))
    parameters.targetBranch.set(getOptionalStringProperty("targetBranch"))
    parameters.originalCommitHash.set(getOptionalStringProperty("originalCommitHash"))
}
