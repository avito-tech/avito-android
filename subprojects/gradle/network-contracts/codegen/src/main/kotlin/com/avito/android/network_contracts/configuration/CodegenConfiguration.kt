package com.avito.android.network_contracts.configuration

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

public class CodegenConfiguration(
    private val original: Configuration
) : Configuration by original {

    internal fun setArtifactsExecutable() {
        original.incoming.afterResolve {
            it.files.forEach { it.setExecutable(true) }
        }
    }

    public companion object {

        public const val NAME: String = "codegen"

        public fun getInstance(project: Project): CodegenConfiguration {
            val configuration = project.configurations.findByName(NAME)
                ?: create(project)

            return CodegenConfiguration(configuration)
        }

        private fun create(project: Project): Configuration {
            return project.configurations.create(NAME) {
                it.isTransitive = false
            }
        }
    }
}

internal val Project.codegenConfiguration: CodegenConfiguration
    get() = CodegenConfiguration.getInstance(this)
