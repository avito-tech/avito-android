package com.avito.android.contracts.platform.dependency

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

public open class CodegenDependencyConfiguration(
    private val original: Configuration
) : Configuration by original {

    internal fun setArtifactsExecutable() {
        original.incoming.afterResolve {
            it.files.forEach { it.setExecutable(true) }
        }
    }

    public companion object {

        public const val NAME: String = "codegen"

        public fun getInstance(project: Project): CodegenDependencyConfiguration {
            val configuration = project.configurations.findByName(NAME)
                ?: create(project)

            return CodegenDependencyConfiguration(configuration)
        }

        private fun create(project: Project): Configuration {
            return project.configurations.create(NAME) {
                it.isTransitive = false
            }
        }
    }
}

internal val Project.codegenDependencyConfiguration: CodegenDependencyConfiguration
    get() = CodegenDependencyConfiguration.getInstance(this)
