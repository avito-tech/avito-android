package com.avito.impact.configuration

import com.avito.android.Result
import com.avito.impact.changes.ChangedFile
import com.avito.impact.configuration.sets.isImplementation
import com.avito.impact.fallback.ImpactFallbackDetector
import com.avito.module.configurations.ConfigurationType.Main

public class MainConfiguration(module: InternalModule) : BaseConfiguration(
    module,
    Main
) {

    override val isModified: Boolean by lazy {
        module.fallbackDetector.isFallback is ImpactFallbackDetector.Result.Skip
            || dependencies.any { it.isModified }
            || hasChangedFiles
    }

    override val dependencies: Set<MainConfiguration> by lazy {
        require(project.configurations.isNotEmpty()) {
            "Configurations of ${project.path} required to continue impact analysis, but nothing found. \n" +
                "Most likely reasons: \n" +
                "- Using impact analysis during gradle configuration phase \n" +
                "- Working with regular directory as with module \n"
        }
        super.dependencies
    }

    override fun changedFiles(): Result<List<ChangedFile>> {
        val excludes = (module.testConfiguration.sourceSets() +
            module.androidTestConfiguration.sourceSets())
            .minus(project.projectDir)

        return changesDetector.computeChanges(
            project.projectDir,
            excludes
        )
    }

    override fun containsSources(
        @Suppress("DEPRECATION")
        sourceSet: com.android.build.gradle.api.AndroidSourceSet
    ): Boolean = sourceSet.isImplementation()

    override fun toString(): String {
        return "ImplementationConfiguration(${project.path})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MainConfiguration
        if (project != other.project) return false
        return true
    }

    override fun hashCode(): Int = project.hashCode()
}
