@file:Suppress("UnstableApiUsage", "MoveVariableDeclarationIntoWhen")

package com.avito.git

import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.lazy
import com.avito.kotlin.dsl.lazyProperty
import org.gradle.api.Project
import org.gradle.api.provider.Provider

@JvmOverloads // for using default parameters in build.gradle
fun Project.gitState(logger: (String) -> Unit = {}): Provider<GitState> = lazyProperty("GIT_STATE_PROVIDER") { project ->
    project.providers.lazy {
        // todo do we need that flag? Could we use BuildEnvironment to make decisions?
        // todo make local by default?
        val strategy = project.getOptionalStringProperty("avito.git.state", default = "env")
        when (strategy) {
            "local" -> GitLocalStateImpl.from(project)
            "env" -> GitStateFromEnvironment.from(project, logger)
            else -> throw RuntimeException("Unknown git state strategy: $strategy")
        }
    }
}
