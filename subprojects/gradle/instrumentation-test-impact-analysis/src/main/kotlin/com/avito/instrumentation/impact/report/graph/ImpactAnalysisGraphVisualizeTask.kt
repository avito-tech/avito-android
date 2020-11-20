package com.avito.instrumentation.impact.report.graph

import com.avito.instrumentation.impact.report.graph.html.CytoscapeEdgeData
import com.avito.instrumentation.impact.report.graph.html.CytoscapeNode
import com.avito.instrumentation.impact.report.graph.html.CytoscapeNodeData
import com.avito.instrumentation.impact.report.graph.html.cytoscapeGraphHtml
import com.avito.instrumentation.impact.report.model.ModuleNode
import com.avito.utils.createOrClear
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ImpactAnalysisGraphVisualizeTask : DefaultTask() {

    @Suppress("ANNOTATION_TARGETS_NON_EXISTENT_ACCESSOR")
    @get:Internal
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    @InputFile
    lateinit var impactAnalysisReportJson: File

    @OutputFile
    lateinit var modulesGraphHtml: File

    @TaskAction
    fun make() {
        val impactAnalysisGraphData: Map<String, ModuleNode> = readImpactAnalysisGraph()

        val graphNodes: Set<CytoscapeNode<CytoscapeNodeData>> = impactAnalysisGraphData.values
            .map {
                CytoscapeNodeData(
                    id = it.name,
                    group = "module",
                    modified = it.implementationConfiguration.isModified.toString(),
                    primaryModified = it.implementationConfiguration.isPrimaryModified.toString()
                )
            }
            .map {
                CytoscapeNode(
                    data = it
                )
            }
            .toSet()

        val graphEdges: Set<CytoscapeNode<CytoscapeEdgeData>> = impactAnalysisGraphData.values
            .flatMap { node ->
                node.implementationConfiguration.dependencies
                    .map { dependency ->
                        node.name to dependency
                    }
            }
            .map { (source, destination) ->
                CytoscapeEdgeData(
                    source = source,
                    target = destination,
                    modified = impactAnalysisGraphData[destination]
                        ?.implementationConfiguration
                        ?.isModified
                        ?.toString() ?: "false",
                    primaryModified = impactAnalysisGraphData[destination]
                        ?.implementationConfiguration
                        ?.isPrimaryModified
                        ?.toString() ?: "false"
                )
            }
            .map {
                CytoscapeNode(
                    data = it
                )
            }
            .toSet()

        modulesGraphHtml.createOrClear()
        modulesGraphHtml.writeText(
            text = cytoscapeGraphHtml(
                gson = gson,
                nodes = graphNodes,
                edges = graphEdges
            )
        )
    }

    private fun readImpactAnalysisGraph(): Map<String, ModuleNode> = gson.fromJson(
        impactAnalysisReportJson.readText(),
        object : TypeToken<Map<String, ModuleNode>>() {}.type
    )
}
