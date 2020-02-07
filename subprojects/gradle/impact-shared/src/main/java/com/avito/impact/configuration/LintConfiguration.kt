package com.avito.impact.configuration

import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import java.io.File

class LintConfiguration(module: InternalModule) : SimpleConfiguration(module) {

    override val dependencies: Set<ImplementationConfiguration> by lazy {
        project.configurations
            .filter { it.isLint() }
            .flatMap { configuration ->
                configuration
                    .dependencies
                    .withType(DefaultProjectDependency::class.java)
            }
            .toSet()
            .map {
                it.dependencyProject
                    .internalModule
                    .implementationConfiguration
            }
            .toSet()
    }

    override val isModified: Boolean by lazy {
        dependencies.any { it.isModified }
            || module.androidTestConfiguration.isModified
            || hasChangedFiles
    }

    override val fullBytecodeSets: Set<File> by lazy {
        bytecodeSets() +
            dependencies.flatMap { it.fullBytecodeSets } +
            module.androidTestConfiguration.fullBytecodeSets
    }

    override fun containsSources(sourceSet: AndroidSourceSet) = false
    override fun containsBytecode(bytecodeDirectory: File) = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LintConfiguration
        if (project != other.project) return false
        return true
    }

    override fun hashCode(): Int {
        return project.hashCode()
    }
}
