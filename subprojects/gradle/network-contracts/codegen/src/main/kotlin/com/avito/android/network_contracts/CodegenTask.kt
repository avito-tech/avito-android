package com.avito.android.network_contracts

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@CacheableTask
internal abstract class CodegenTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    @get:Input
    abstract val kind: Property<String>

    @get:Input
    abstract val projectName: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val codegen: Property<RegularFile> = objects.fileProperty()

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemes: Property<FileCollection> = objects.property()

    @get:OutputFiles
    val generatedFilesDirectory: DirectoryProperty = objects.directoryProperty()

    @TaskAction
    fun generate() {
        // TODO: will be implemented in https://jr.avito.ru/browse/MA-3631
    }

    companion object {
        const val NAME = "codegen"
    }
}
