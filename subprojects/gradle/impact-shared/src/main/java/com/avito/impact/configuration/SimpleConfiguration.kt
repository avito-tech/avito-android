@file:Suppress("DEPRECATION")
// todo use new api?

package com.avito.impact.configuration

import com.android.build.gradle.api.AndroidSourceSet
import com.avito.android.androidBaseExtension
import com.avito.android.isAndroid
import com.avito.impact.changes.ChangedFile
import com.avito.impact.util.Equality
import org.funktionale.tries.Try
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

/**
 * Wrapper above [org.gradle.api.artifacts.Configuration] to reduce an amount of configurations
 */
abstract class SimpleConfiguration(val module: InternalModule) : Equality {

    abstract val dependencies: Set<ImplementationConfiguration>
    abstract val fullBytecodeSets: Set<File>
    abstract val isModified: Boolean
    protected val project = module.project
    protected val changesDetector = module.changesDetector
    val path: String = project.path

    open val hasChangedFiles: Boolean by lazy {
        changedFiles()
            .map { it.isNotEmpty() }
            .onFailure {
                project.logger.error("Can't find changes", it)
            }
            .getOrElse { true }
    }

    fun allDependencies(includeSelf: Boolean = true): Set<SimpleConfiguration> {
        val dependencies = this.dependencies
            .flatMap {
                it.allDependencies(includeSelf = true)
            }
            .toSet()

        return if (includeSelf) {
            dependencies.plus(this)
        } else {
            dependencies
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

    open fun changedFiles(): Try<List<ChangedFile>> {
        return sourceSets()
            .map { sourceDir -> changesDetector.computeChanges(sourceDir) }
            .fold(Try { listOf<ChangedFile>() }) { accumulator, element ->
                Try { accumulator.get() + element.get() }
            }
    }

    /**
     * Directories with compiled classes
     */
    fun bytecodeSets(): Set<File> {
        val kotlinCompileTasks = project.tasks.withType(KotlinCompile::class.java)
            .filter { it !is KaptGenerateStubsTask }

        return kotlinCompileTasks
            .map {
                it.destinationDir
            }
            .filter {
                it.isDirectory &&
                    it.exists() &&
                    it.list().isNotEmpty()
            }
            .filter { containsBytecode(it) }
            .toSet()
    }

    protected abstract fun containsBytecode(bytecodeDirectory: File): Boolean
    protected abstract fun containsSources(sourceSet: AndroidSourceSet): Boolean
}

internal fun SimpleConfiguration.dependencies(filter: (Configuration) -> Boolean): Set<ImplementationConfiguration> {
    return module.project.configurations
        .filter(filter)
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
        .toMutableSet().apply {
            remove(this@dependencies) // project has dependency to itself in a default configuration
        }
}
