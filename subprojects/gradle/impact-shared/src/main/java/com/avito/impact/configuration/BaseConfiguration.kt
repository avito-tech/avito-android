@file:Suppress("DEPRECATION")
// todo use new api?

package com.avito.impact.configuration

import com.android.build.gradle.api.AndroidSourceSet
import com.avito.android.androidBaseExtension
import com.avito.android.isAndroid
import com.avito.impact.changes.ChangedFile
import com.avito.impact.util.Equality
import com.avito.module.configurations.ConfigurationType
import com.avito.module.dependencies.dependenciesOnProjects
import org.funktionale.tries.Try
import org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

/**
 * Wrapper above [org.gradle.api.artifacts.Configuration] to reduce an amount of configurations
 */
abstract class BaseConfiguration(
    val module: InternalModule,
    val types: Set<Class<out ConfigurationType>>
) : Equality {

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

    open val dependencies: Set<MainConfiguration> by lazy {
        module.project.dependenciesOnProjects(types)
            .map {
                it.dependencyProject
                    .internalModule
                    .mainConfiguration
            }
            .toSet()
    }

    fun allDependencies(includeSelf: Boolean = true): Set<BaseConfiguration> {
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

    open fun changedFiles(): Try<List<ChangedFile>> {
        return sourceSets()
            .map { sourceDir -> changesDetector.computeChanges(sourceDir) }
            .fold(Try { listOf() }) { accumulator, element ->
                Try { accumulator.get() + element.get() }
            }
    }

    protected abstract fun containsBytecode(bytecodeDirectory: File): Boolean
    protected abstract fun containsSources(sourceSet: AndroidSourceSet): Boolean
}
