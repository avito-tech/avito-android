package com.avito.module.metrics

import com.avito.module.dependencies.graphbuilder.SimpleModuleGraph
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jgrapht.alg.scoring.BetweennessCentrality
import org.jgrapht.alg.scoring.BetweennessCentrality.OverflowStrategy.THROW_EXCEPTION_ON_OVERFLOW

public abstract class CollectModuleBetweennessCentralityTask : DefaultTask() {

    init {
        description = "Collect betweenness centrality metric for every module"
    }

    @get:OutputFile
    public abstract val output: RegularFileProperty

    @TaskAction
    public fun action() {
        val graph = SimpleModuleGraph().compute(project)
        val betweennessCentrality = BetweennessCentrality(graph, false, THROW_EXCEPTION_ON_OVERFLOW)

        CsvMapper().writer().writeValues(output.asFile.get()).use { writer ->
            writer.write(arrayOf("module", "betweenness-centrality"))

            betweennessCentrality.scores
                .toList()
                .sortedByDescending { it.second }
                .forEach { writer.write(arrayOf(it.first.path, it.second.toString())) }
        }
    }
}
