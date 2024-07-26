package com.avito.android.module_type.validation.configurations.missings.implementations

import com.avito.android.module_type.validation.configurations.missings.implementations.internal.DependenciesFileReader
import com.avito.android.module_type.validation.configurations.missings.implementations.internal.MissingPublicDependencies
import com.avito.android.module_type.validation.configurations.missings.implementations.internal.ProjectDependencyInfo
import com.avito.android.module_type.validation.configurations.missings.implementations.internal.isPublicType
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
public abstract class MissingImplementationDependencyTask : DefaultTask() {

    @get:Input
    public abstract val projectPath: Property<String>

    @get:Input
    public abstract val buildFileRelativePath: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val projectDependencies: Property<File>

    @get:OutputFile
    internal abstract val reportFile: RegularFileProperty

    @TaskAction
    public fun check() {
        val dependenciesFileReader =
            DependenciesFileReader(projectDependencies.get(), projectPath.get())

        val publicWithoutImpl = dependenciesFileReader.readProjectDependencies()
            .groupBy { it.logicalModule }
            .filterValues { modules -> modules.all(ProjectDependencyInfo::isPublicType) }

        writeReport(publicWithoutImpl)
    }

    private fun writeReport(publicWithoutImpl: Map<String, List<ProjectDependencyInfo>>) {
        val projectWithNotImplementationDependenciesAdapter = MissingPublicDependencies.adapter()

        val missingPublicDependencies = MissingPublicDependencies(
            projectPath = projectPath.get(),
            buildFilePath = buildFileRelativePath.get(),
            dependencies = publicWithoutImpl
        )
        val serializedReport = projectWithNotImplementationDependenciesAdapter.toJson(missingPublicDependencies)
        reportFile.get().asFile.writeText(serializedReport)
    }

    public companion object {
        public const val NAME: String = "validateMissingImplementations"
    }
}
