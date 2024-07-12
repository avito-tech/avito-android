package com.avito.android.module_type.validation.configurations.missings.implementations

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

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
    public abstract val errorMessages: ConfigurableFileCollection

    @get:OutputFile
    public abstract val outputFile: RegularFileProperty

    @TaskAction
    public fun validate() {
        val errorMessage = errorMessages.files
            .filter { it.exists() }
            .map { it.readText().trim() }
            .filter(String::isNotEmpty)
            .joinToString(separator = "\n\n-------------------\n\n")

        if (errorMessage.isEmpty()) {
            outputFile.get().asFile.writeText("OK")
        } else {
            val placeholder = """
                Unable to find implementations (:impl or :fake) for all :public modules.
                See docs for details: https://links.k.avito.ru/android-missing-implementation-dependency
                
                
            """.trimIndent()
            error(placeholder + errorMessage)
        }
    }
}
