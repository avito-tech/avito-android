package com.avito.android.module_type.internal

import com.avito.android.module_type.ModuleWithType
import com.avito.module.configurations.ConfigurationType
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * This task is a workaround to satisfy project isolation restrictions.
 * We can't reference project directly while configuring other tasks.
 */
internal abstract class ExtractModuleDescriptionTask : DefaultTask() {

    @get:Input
    public abstract val module: Property<ModuleWithType>

    @get:Input
    public abstract val directDependencies: MapProperty<ConfigurationType, Set<String>>

    @get:OutputFile
    public abstract val outputFile: RegularFileProperty

    @TaskAction
    public fun doAction() {
        val output = outputFile.get().asFile
        if (!output.exists()) {
            output.createNewFile()
        }
        val description = ModuleDescription(
            module = module.get(),
            directDependencies = directDependencies.get()
        )
        FileSerializer.write(description, output)
    }

    internal companion object {
        const val name = "extractModuleDescription"
        const val outputPath = "outputs/module-types/moduleDescriptor.bin"
    }
}
