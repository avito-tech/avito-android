package com.avito.android.module_graph

import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile

public interface ModuleGraphTask : Task {

    @get:OutputFile
    public val outputFile: RegularFileProperty
}
