@file:Suppress("MoveVariableDeclarationIntoWhen")

package com.avito.git

import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.lazyProperty
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

/**
 * Warning! used in build.gradle files
 */
public fun Project.gitState(): Property<GitState> =
    lazyProperty("GIT_STATE_PROVIDER") { project ->
        project.objects.property<GitState>().apply {
            val strategy = project.getOptionalStringProperty("avito.git.state", default = "local")
            set(
                when (strategy) {
                    "local" -> GitLocalStateImpl.from(
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
