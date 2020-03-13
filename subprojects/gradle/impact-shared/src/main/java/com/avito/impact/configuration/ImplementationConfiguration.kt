package com.avito.impact.configuration

import com.android.build.gradle.api.AndroidSourceSet
import com.avito.impact.changes.ChangedFile
import com.avito.impact.configuration.sets.isImplementation
import com.avito.impact.fallback.ImpactFallbackDetector
import org.funktionale.tries.Try
import org.gradle.api.attributes.Category
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import java.io.File

/**
 * todo надо переименовать, кажется что речь про configuration: implementation, а по факту это гораздо большее число конфигураций
 */
class ImplementationConfiguration(module: InternalModule) : SimpleConfiguration(module) {

    override val isModified: Boolean by lazy {
        (module.fallbackDetector.isFallback is ImpactFallbackDetector.Result.Skip)
            || dependencies.any { it.isModified }
            || hasChangedFiles
    }

    override val fullBytecodeSets: Set<File> by lazy {
        bytecodeSets() +
            dependencies.flatMap { it.fullBytecodeSets }
    }

    override val hasChangedFiles: Boolean by lazy {
        changedFiles()
            .map { it.isNotEmpty() }
            .onFailure {
                project.logger.error("Can't find changes", it)
            }
            .getOrElse { true }
    }

    override fun changedFiles(): Try<List<ChangedFile>> {
        val excludes = (module.testConfiguration.sourceSets() +
            module.androidTestConfiguration.sourceSets())
            .minus(project.projectDir)

        return changesDetector.computeChanges(
            project.projectDir,
            excludes
        )
    }

    override val dependencies: Set<ImplementationConfiguration> by lazy {
        require(project.configurations.isNotEmpty()) {
            "configurations of ${project.path} must be available here. \n" +
                "Most likely reasons: \n" +
                "- Using impact analysis during gradle configuration phase \n" +
                "- Working with regular directory as with module \n"
        }
        project.configurations
            .filter { it.isImplementation() }
            .flatMap { configuration ->
                configuration
                    .dependencies
                    .withType(DefaultProjectDependency::class.java)
            }
            .filter {
                val category = it.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name
                category == null || category == Category.LIBRARY
            }
            .toSet()
            .map {
                it.dependencyProject
                    .internalModule
                    .implementationConfiguration
            }
            .toSet()
    }

    override fun containsSources(sourceSet: AndroidSourceSet) = sourceSet.isImplementation()
    override fun containsBytecode(bytecodeDirectory: File): Boolean = bytecodeDirectory.isImplementation()

    override fun toString(): String {
        return "ImplementationConfiguration(${project.path})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ImplementationConfiguration
        if (project != other.project) return false
        return true
    }

    override fun hashCode(): Int = project.hashCode()

}
