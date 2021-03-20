@file:Suppress("DEPRECATION")
// todo use new api?

package com.avito.impact.configuration

import com.android.build.gradle.api.AndroidSourceSet
import com.avito.android.Result
import com.avito.impact.changes.ChangedFile
import com.avito.impact.configuration.sets.isImplementation
import com.avito.impact.fallback.ImpactFallbackDetector
import com.avito.module.configurations.ConfigurationType.Main

class MainConfiguration(module: InternalModule) : BaseConfiguration(
    module,
    setOf(
        Main::class.java
    )
) {

    override val isModified: Boolean by lazy {
        module.fallbackDetector.isFallback is ImpactFallbackDetector.Result.Skip
            || dependencies.any { it.isModified }
            || hasChangedFiles
    }

    override val hasChangedFiles: Boolean by lazy {
        changedFiles()
            .map { it.isNotEmpty() }
            .onFailure {
                project.logger.error("Can't find changes", it)
            }
            .getOrElse { true }
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

    override fun containsSources(sourceSet: AndroidSourceSet) = sourceSet.isImplementation()

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
