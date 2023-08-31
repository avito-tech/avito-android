package com.avito.android.module_type.validation.configurations.missings.implementations

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.validation.configurations.missings.implementations.internal.MissingPublicDependencies
import com.avito.android.module_type.validation.configurations.missings.implementations.internal.ProjectDependencyInfo
import com.avito.android.module_type.validation.configurations.missings.implementations.internal.asRegex
import com.avito.android.module_type.validation.configurations.missings.implementations.internal.isPublicType
import com.avito.capitalize
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * The task checks if the module has implementations of all public dependencies.
 *
 * Applications or demo module must have all implementations of public dependencies,
 * otherwise we may encounter unrecognized problems like ClassNotFoundException, etc. at runtime.
 * This can happen, for example, due to Anvil, when we connect an implementation by the ContributesTo annotation
 * inside an impl module, but do not connect it.
 *
 * This check is intended to help at the customization stage to avoid such problems.
 * You can find more information about patterns in the documentation:
 *
 * https://docs.k.avito.ru/mobile/android/architecture/modules-2/Patterns/
 */
@CacheableTask
public abstract class MissingImplementationDependencyRootTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val reports: ConfigurableFileCollection

    @get:Internal
    public abstract val rootProjectDir: Property<File>

    @get:OutputFile
    public abstract val outputFile: RegularFileProperty

    @TaskAction
    public fun check() {
        val adapter = MissingPublicDependencies.adapter()
        val errorMessage = reports.files
            .mapNotNull { adapter.fromJson(it.readText()) }
            .map(::createModuleErrorReport)
            .filter(String::isNotBlank)
            .joinToString(separator = "\n-------------------\n")

        if (errorMessage.isEmpty()) {
            outputFile.get().asFile.writeText("OK")
        } else {
            val placeHolder = "Public dependencies validation failed. Unable to find implementation for all public.\n"
            error(placeHolder + errorMessage)
        }
    }

    private fun createModuleErrorReport(
        missingDependenciesInfo: MissingPublicDependencies
    ): String {
        val buildFilePath = File(rootProjectDir.get(), missingDependenciesInfo.buildFilePath)
        val dependencies = missingDependenciesInfo.dependencies
        if (dependencies.isEmpty()) {
            return ""
        }

        val errorText = buildString {
            appendLine("Please, add impl/fake dependencies to the build file")
            appendLine("file://" + buildFilePath.absolutePath)
            appendLine(
                dependencies.entries.joinToString(transform = ::createDetailedInformation)
            )
        }
        return errorText
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
        }
    }

    private fun findPublicImplementationModules(logicalModule: String): List<String> {
        val implementationTypes = setOf(FunctionalType.Impl, FunctionalType.Fake)

        val logicalModuleDirectory = File(
            rootProjectDir.get(),
            logicalModule.removePrefix(":").replace(":", File.pathSeparator)
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
