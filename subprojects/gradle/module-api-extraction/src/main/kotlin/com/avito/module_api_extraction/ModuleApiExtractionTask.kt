package com.avito.module_api_extraction

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
internal abstract class ModuleApiExtractionTask : DefaultTask() {
    @get:Input
    abstract val moduleNames: ListProperty<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val syntheticProjectJsonFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputJson: RegularFileProperty

    @TaskAction
    fun extract() {
        val extractor = ModuleApiExtractor()
        val outputJsonText = extractor.extract(moduleNames.get(), syntheticProjectJsonFiles.files)
        outputJson.get().asFile.writeText(outputJsonText)
    }
}
