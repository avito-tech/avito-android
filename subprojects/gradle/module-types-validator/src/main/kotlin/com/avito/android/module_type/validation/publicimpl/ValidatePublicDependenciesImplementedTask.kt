package com.avito.android.module_type.validation.publicimpl

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.validation.publicimpl.internal.DependenciesFileReader
import com.avito.android.module_type.validation.publicimpl.internal.ProjectDependencyInfo
import com.avito.android.module_type.validation.publicimpl.internal.asRegex
import com.avito.capitalize
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
public abstract class ValidatePublicDependenciesImplementedTask : DefaultTask() {

    @get:Input
    public abstract val functionalType: Property<FunctionalType>

    @get:Input
    public abstract val projectPath: Property<String>

    @get:Internal
    public abstract val rootProjectDir: Property<File>

    @get:Internal
    public abstract val buildFile: Property<File>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val projectDependencies: Property<File>

    @get:OutputFile
    internal abstract val reportFile: RegularFileProperty

    @TaskAction
    public fun check() {
        require(functionalType.get() == FunctionalType.Application) {
            "This validation check should be performed only on demo or application modules"
        }

        val dependenciesFileReader =
            DependenciesFileReader(projectDependencies.get(), projectPath.get())

        val publicWithoutImpl = dependenciesFileReader.readProjectDependencies()
            .groupBy { it.logicalModule }
            .filterValues { modules -> modules.all(ProjectDependencyInfo::isPublicType) }

        if (publicWithoutImpl.isNotEmpty()) {
            reportError(publicWithoutImpl)
        }
    }

    private fun reportError(publicModules: Map<String, List<ProjectDependencyInfo>>) {
        val errorText = buildString {
            appendLine("Public dependencies validation failed. Unable to find implementation for all public.")
            appendLine()
            appendLine(publicModules.entries.joinToString(transform = ::createDetailedInformation))
        }

        reportFile.get().asFile.writeText(errorText)
        error(errorText)
    }

    private fun createDetailedInformation(dependency: Map.Entry<String, List<ProjectDependencyInfo>>): String {
        val (logicalModule, availablePaths) = dependency
        val fullModulePath = availablePaths.first(ProjectDependencyInfo::isPublicType).modulePath
        val possibleImplementationModules = findPublicImplementationModules(logicalModule)
        val breadcrumbsPaths = availablePaths.joinToString(separator = "\n") { it.fullPath }

        val possibleImplementations = possibleImplementationModules.joinToString(separator = "\n") { modulePath ->
            val projectReferenceText = convertModulePathToProjectReference(modulePath)
            "\timplementation(projects.$projectReferenceText)"
        }

        return buildString {
            appendLine("Please, add impl/fake dependencies to the build file")
            appendLine("file://" + buildFile.get().absolutePath)
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

    public companion object {
        public const val NAME: String = "validatePublicDependenciesImplemented"
    }
}

private fun ProjectDependencyInfo.isPublicType(): Boolean {
    return functionalType == FunctionalType.Public
}
