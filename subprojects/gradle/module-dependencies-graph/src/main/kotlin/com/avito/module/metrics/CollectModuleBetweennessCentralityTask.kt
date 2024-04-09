package com.avito.module.metrics

import com.avito.android.CodeOwnershipExtension
import com.avito.android.model.Owner
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.findByType

public abstract class CollectModuleBetweennessCentralityTask : DefaultTask() {

    init {
        description = "Collect betweenness centrality metric for every module"
    }

    @get:OutputFile
    public abstract val moduleGraphOutput: RegularFileProperty

    @get:OutputFile
    public abstract val betweennessCentralityOutput: RegularFileProperty

    private val Project.owners: Set<Owner>
        get() = this.extensions.findByType<CodeOwnershipExtension>()?.owners?.get().orEmpty()

    @TaskAction
    public fun action() {
        val actionOutput = CollectModuleBetweennessCentralityAction().collect(project)

        CsvMapper().writer().writeValues(moduleGraphOutput.asFile.get()).use { writer ->
            writer.write(arrayOf("from", "to"))

            val moduleGraph = actionOutput.moduleGraph

            moduleGraph
                .edgeSet()
                .map { moduleGraph.getEdgeSource(it) to moduleGraph.getEdgeTarget(it) }
                .sortedWith(compareBy({ it.first }, { it.second }))
                .forEach { (from, to) ->
                    writer.write(arrayOf(
                        from.path,
                        to.path,
                    ))
                }
        }

        CsvMapper().writer().writeValues(betweennessCentralityOutput.asFile.get()).use { writer ->
            writer.write(arrayOf("module", "betweenness-centrality", "owners"))

            actionOutput
                .betweennessCentrality
                .toList()
                .sortedWith(compareByDescending<Pair<Project, Double>> { it.second }.thenBy { it.first })
                .forEach { (project, betweennessCentrality) ->
                    writer.write(arrayOf(
                        project.path,
                        betweennessCentrality.toString(),
                        project.owners.joinToString(", "),
                    ))
                }
        }
    }

    public companion object {
        public const val OUTPUT_GRAPH_PATH: String = "reports/modules-betweenness-centrality/modules-graph.csv"
        public const val OUTPUT_BETWEENNESS_CENTRALITY_PATH: String =
            "reports/modules-betweenness-centrality/modules-betweenness-centrality.csv"
    }
}
