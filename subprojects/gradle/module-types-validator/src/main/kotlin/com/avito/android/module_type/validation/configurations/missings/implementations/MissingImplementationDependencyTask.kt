package com.avito.android.module_type.validation.configurations.missings.implementations

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.validation.configurations.missings.implementations.internal.MissingImplementationDependencyTaskDelegate
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
    public abstract val appModulePath: Property<String>

    @get:Input
    public abstract val appModuleBuildFilePath: Property<String>

    @get:Input
    public abstract val appModuleType: Property<FunctionalType>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val projectsTaskOutput: Property<File>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val appDependencies: Property<File>

    @get:OutputFile
    internal abstract val outputStatusFile: RegularFileProperty

    @get:OutputFile
    internal abstract val outputErrorMessageFile: RegularFileProperty

    @TaskAction
    public fun validate() {
        val result = MissingImplementationDependencyTaskDelegate().validate(
            appModulePath = appModulePath.get(),
            appModuleBuildFilePath = appModuleBuildFilePath.get(),
            appModuleType = appModuleType.get(),
            projectsTaskOutputText = projectsTaskOutput.get().readText(),
            appDependenciesText = appDependencies.get().readText(),
        )

        result.onSuccess {
            outputStatusFile.get().asFile.writeText("Success")
        }.onFailure {
            outputStatusFile.get().asFile.writeText("Error")
            outputErrorMessageFile.get().asFile.writeText(it.message!!)
        }
    }

    public companion object {
        public const val NAME: String = "validateMissingImplementations"
    }
}
