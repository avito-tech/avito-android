package com.avito.impact.configuration

import com.avito.android.Result
import com.avito.impact.changes.ChangedFile
import com.avito.impact.configuration.sets.isImplementation
import com.avito.impact.fallback.ImpactFallbackDetector
import com.avito.module.configurations.ConfigurationType.CodeGenerators

public class CodeGeneratorsConfiguration(module: InternalModule) : BaseConfiguration(
    module, CodeGenerators
) {

    override val isModified: Boolean by lazy {
        module.fallbackDetector.isFallback is ImpactFallbackDetector.Result.Skip
            || dependencies.any { it.isModified }
            || hasChangedFiles
    }

    override fun changedFiles(): Result<List<ChangedFile>> {
        val excludes = module.testConfiguration.sourceSets().minus(project.projectDir)

        return changesDetector.computeChanges(
            project.projectDir, excludes
        )
    }

    override fun containsSources(
        @Suppress("DEPRECATION") sourceSet: com.android.build.gradle.api.AndroidSourceSet
    ): Boolean = sourceSet.isImplementation()

    override fun toString(): String {
        return "CodeGeneratorsConfiguration(${project.path})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CodeGeneratorsConfiguration
        if (project != other.project) return false
        return true
    }

    override fun hashCode(): Int = project.hashCode()
}
