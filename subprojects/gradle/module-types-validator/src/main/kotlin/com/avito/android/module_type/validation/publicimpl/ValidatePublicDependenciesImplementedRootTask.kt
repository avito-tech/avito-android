package com.avito.android.module_type.validation.publicimpl

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.validation.publicimpl.internal.MissingPublicDependencies
import com.avito.android.module_type.validation.publicimpl.internal.ProjectDependencyInfo
import com.avito.android.module_type.validation.publicimpl.internal.asRegex
import com.avito.android.module_type.validation.publicimpl.internal.isPublicType
import com.avito.capitalize
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

public abstract class ValidatePublicDependenciesImplementedRootTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val reports: ConfigurableFileCollection

    @get:Internal
    public abstract val rootProjectDir: Property<File>

    @TaskAction
    public fun check() {
        val adapter = MissingPublicDependencies.adapter()
        reports.files
            .mapNotNull { adapter.fromJson(it.readText()) }
            .forEach(::reportError)
    }

    private fun reportError(
        missingDependenciesInfo: MissingPublicDependencies
    ) {
        val buildFilePath = File(rootProjectDir.get(), missingDependenciesInfo.buildFilePath)
        val dependencies = missingDependenciesInfo.dependencies
        if (dependencies.isEmpty()) {
            return
        }

        val errorText = buildString {
            appendLine("Public dependencies validation failed. Unable to find implementation for all public.")
            appendLine()
            appendLine("Please, add impl/fake dependencies to the build file")
            appendLine("file://" + buildFilePath.absolutePath)
            appendLine(
                dependencies.entries.joinToString(transform = ::createDetailedInformation)
            )
        }

        error(errorText)
    }

    private fun createDetailedInformation(
        dependency: Map.Entry<String, List<ProjectDependencyInfo>>
    ): String {
        val (logicalModule, availablePaths) = dependency
        val fullModulePath = availablePaths.first(ProjectDependencyInfo::isPublicType).modulePath
        val possibleImplementationModules = findPublicImplementationModules(logicalModule)
        val breadcrumbsPaths = availablePaths.joinToString(separator = "\n") { it.fullPath }

        val possibleImplementations = possibleImplementationModules.joinToString(separator = "\n") { modulePath ->
            val projectReferenceText = convertModulePathToProjectReference(modulePath)
            "\timplementation(projects.$projectReferenceText)"
        }

        return buildString {
            appendLine("Possible implementations:")
            appendLine(possibleImplementations)
            appendLine()
            appendLine("Missing implementations for module:")
            appendLine("\t **$fullModulePath**")
            appendLine("Dependency appears from: ")
            appendLine("\t" + breadcrumbsPaths)
            appendLine("-------")
        }
    }

    private fun findPublicImplementationModules(logicalModule: String): List<String> {
        val implementationTypes = setOf(FunctionalType.Impl, FunctionalType.Fake)

        val logicalModuleDirectory = File(
            rootProjectDir.get(),
            logicalModule.removePrefix(":").replace(":", "/")
        )

        if (!logicalModuleDirectory.exists()) {
            return emptyList()
        }

        return logicalModuleDirectory
            .listFiles()
            .orEmpty()
            .filter { it.name.matches(implementationTypes.asRegex()) }
            .map { "$logicalModule:${it.name}" }
    }

    private fun convertModulePathToProjectReference(modulePath: String): String {
        return modulePath
            .removePrefix(":")
            .split(":").joinToString(separator = ".") { moduleName ->
                moduleName.split("-")
                    .joinToString(separator = "") { it.capitalize() }
                    .replaceFirstChar { it.lowercase() }
            }
    }
}
