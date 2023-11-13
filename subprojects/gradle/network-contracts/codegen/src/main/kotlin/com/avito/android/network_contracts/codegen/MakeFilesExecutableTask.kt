package com.avito.android.network_contracts.codegen

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

@CacheableTask
internal abstract class MakeFilesExecutableTask : DefaultTask() {

    @get:OutputFiles
    abstract val files: ConfigurableFileCollection

    @TaskAction
    fun action() {
        files.files.forEach { file ->
            file.setExecutable(true)
        }
    }

    companion object {
        const val NAME = "makeFilesExecutable"
    }
}
