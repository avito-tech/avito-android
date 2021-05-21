@file:Suppress("DEPRECATION")
// todo use new api?

package com.avito.impact.configuration

import com.android.build.gradle.api.AndroidSourceSet
import com.avito.android.Result
import com.avito.android.androidBaseExtension
import com.avito.android.isAndroid
import com.avito.impact.changes.ChangedFile
import com.avito.impact.util.Equality
import com.avito.module.configurations.ConfigurationType
import com.avito.module.dependencies.dependenciesOnProjects
import java.io.File

/**
 * Wrapper above [org.gradle.api.artifacts.Configuration] to reduce an amount of configurations
 */
abstract class BaseConfiguration(
    val module: InternalModule,
    val type: Class<out ConfigurationType>
) : Equality {

    abstract val isModified: Boolean
    protected val project = module.project
    protected val changesDetector = module.changesDetector
    val path: String = project.path

    val hasChangedFiles: Boolean by lazy {
        changedFiles()
            .map { it.isNotEmpty() }
            .onFailure {
                project.logger.error("Can't find changes", it)
            }
            .getOrElse { true }
    }

    open val dependencies: Set<MainConfiguration> by lazy {
        module.project.dependenciesOnProjects(setOf(type))
            .map {
                it.dependencyProject
                    .internalModule
                    .mainConfiguration
            }
            .toSet()
    }

    fun allDependencies(includeSelf: Boolean = true): Set<BaseConfiguration> {
        val dependencies = mutableSetOf<BaseConfiguration>()
        val visited = mutableSetOf<BaseConfiguration>()
        this.traverseDependencies(visited) { conf: BaseConfiguration ->
            dependencies.add(conf)
        }
        return if (includeSelf) {
            dependencies.plus(this)
        } else {
            dependencies
        }
    }

    @Suppress("unused") // false-positive for receiver
    private fun BaseConfiguration.traverseDependencies(
        visited: MutableSet<BaseConfiguration>,
        visitor: (BaseConfiguration) -> Unit
    ) {
        this.dependencies
            .forEach { node ->
                if (!visited.contains(node)) {
                    visitor(node)
                    node.traverseDependencies(visited, visitor)
                    visited.add(node)
                }
            }
    }

    fun sourceSets(): Set<File> {
        return if (project.isAndroid()) {
            project.androidBaseExtension.sourceSets
                .filter { containsSources(it) }
                .flatMap { it.java.srcDirs }
                .map { File(it.canonicalPath.substringBeforeLast("java")) }
                .filter { it.exists() }
                .toSet()
        } else {
            setOf(project.projectDir) // TODO find source sets
        }
    }

    open fun changedFiles(): Result<List<ChangedFile>> {
        return sourceSets()
            .map { sourceDir -> changesDetector.computeChanges(sourceDir) }
            .fold(Result.tryCatch { listOf() }) { accumulator, element ->
                Result.tryCatch { accumulator.getOrThrow() + element.getOrThrow() }
            }
    }

    protected abstract fun containsSources(sourceSet: AndroidSourceSet): Boolean
}
