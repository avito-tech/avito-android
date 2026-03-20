package com.avito.android.module_type.validation.configurations.forbidden.demo_dependencies

import com.avito.android.module_type.validation.configurations.forbidden.demo_dependencies.internal.ForbiddenDemoDependenciesTaskDelegate
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
public abstract class ForbiddenDemoDependenciesTask : DefaultTask() {

    @get:Input
    public abstract val appModulePath: Property<String>

    @get:Input
    public abstract val appModuleBuildFilePath: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val appDependenciesFile: Property<File>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val forbiddenDependenciesFile: RegularFileProperty

    @get:Input
    public abstract val allowedDependencies: SetProperty<String>

    @get:OutputFile
    internal abstract val outputStatusFile: RegularFileProperty

    @TaskAction
    public fun validate() {
        ForbiddenDemoDependenciesTaskDelegate().validate(
            appModulePath = appModulePath.get(),
            appModuleBuildFilePath = appModuleBuildFilePath.get(),
            appDependenciesText = appDependenciesFile.get().readText(),
            forbiddenDependenciesText = forbiddenDependenciesFile.get().asFile.readText(),
            allowedDependencies = allowedDependencies.get(),
        ).onFailure {
            throw GradleException(it.message!!)
        }

        outputStatusFile.get().asFile.writeText("Success")
    }

    public companion object {
        public const val NAME: String = "validateForbiddenDemoDependencies"
    }
}
