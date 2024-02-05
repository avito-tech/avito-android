package com.avito.module.metrics

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

public abstract class CollectModuleBetweennessCentralityTask : DefaultTask() {

    init {
        description = "Collect betweenness centrality metric for every module"
    }

    @get:OutputFile
    public abstract val output: RegularFileProperty

    @TaskAction
    public fun action() {
        val betweennessCentrality = CollectModuleBetweennessCentralityAction().collect(project)

        CsvMapper().writer().writeValues(output.asFile.get()).use { writer ->
            writer.write(arrayOf("module", "betweenness-centrality"))

            betweennessCentrality
                .toList()
                .sortedByDescending { it.second }
                .forEach { writer.write(arrayOf(it.first.path, it.second.toString())) }
        }
    }
}
