package com.avito.android.module_type.validation.publicimpl

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.validation.publicimpl.internal.DependenciesFileReader
import com.avito.android.module_type.validation.publicimpl.internal.ProjectDependencyInfo
import com.avito.android.module_type.validation.publicimpl.internal.asRegex
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
            appendLine(
                "Project ${projectPath.get()} must include all implementations/fakes " +
                        "for public dependencies of logical modules.\n" +
                        publicModules.entries.joinToString(transform = ::createDetailedInformation)
            )
            appendLine("------\n")
        }

        reportFile.get().asFile.writeText(errorText)
        error(errorText)
    }

    private fun createDetailedInformation(dependency: Map.Entry<String, List<ProjectDependencyInfo>>): String {
        val (logicalModule, availablePaths) = dependency
        val fullModulePath = availablePaths.first(ProjectDependencyInfo::isPublicType).modulePath
        val possibleImplementations = findPublicImplementation(logicalModule)
        val breadcrumbsPaths = availablePaths.joinToString(separator = "\n") { it.fullPath }

        return buildString {
            appendLine("Such modules are: * $fullModulePath")
                .appendLine("Dependency appears from: ")
                .appendLine(breadcrumbsPaths)
                .appendLine()
                .appendLine("Possible implementations: $possibleImplementations")
        }
    }

    private fun findPublicImplementation(logicalModule: String): List<String> {
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

    public companion object {
        public const val NAME: String = "validatePublicDependenciesImplemented"
    }
}

private fun ProjectDependencyInfo.isPublicType(): Boolean {
    return functionalType == FunctionalType.Public
}
