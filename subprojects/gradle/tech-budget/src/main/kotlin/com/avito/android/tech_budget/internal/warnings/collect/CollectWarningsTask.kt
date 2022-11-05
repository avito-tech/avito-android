package com.avito.android.tech_budget.internal.warnings.collect

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

internal abstract class CollectWarningsTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun doCollect() {
        // Do nothing, collect is performed in TaskLogsSaver
    }

    companion object {
        const val NAME = "collectWarnings"
    }
}
