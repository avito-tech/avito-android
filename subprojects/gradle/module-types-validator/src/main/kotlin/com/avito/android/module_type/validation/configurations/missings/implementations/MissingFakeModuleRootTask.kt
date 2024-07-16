package com.avito.android.module_type.validation.configurations.missings.implementations

import com.avito.android.module_type.validation.configurations.missings.implementations.internal.MissingFakeModuleRootTaskDelegate
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * The task checks whether all logical modules that have :public and :impl modules
 * also have a :fake module
 */
@CacheableTask
public abstract class MissingFakeModuleRootTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val projectsTaskOutput: Property<File>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val ignoreLogicalModulesRegexes: Property<File>

    @get:OutputFile
    public abstract val outputFile: RegularFileProperty

    @TaskAction
    public fun check() {
        val result = MissingFakeModuleRootTaskDelegate().validate(
            projectsTaskOutputText = projectsTaskOutput.get().readText(),
            ignoreLogicalModuleRegexesText = ignoreLogicalModulesRegexes.get().readText(),
        )

        result.onSuccess {
            outputFile.get().asFile.writeText("Success")
        }.onFailure {
            error(it)
        }
    }

    public companion object {
        public const val NAME: String = "validateMissingFakeModules"
    }
}
