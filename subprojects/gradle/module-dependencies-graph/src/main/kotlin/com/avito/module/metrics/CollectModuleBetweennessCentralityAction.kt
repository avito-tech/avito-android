package com.avito.module.metrics

import com.avito.kotlin.dsl.isRoot
import com.avito.module.dependencies.graphbuilder.SimpleModuleGraph
import org.gradle.api.Project
import org.jgrapht.alg.scoring.BetweennessCentrality
import org.jgrapht.alg.scoring.BetweennessCentrality.OverflowStrategy.THROW_EXCEPTION_ON_OVERFLOW
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph

public class CollectModuleBetweennessCentralityAction {

    public fun collect(project: Project): Output {
        require(project.isRoot())

        val moduleGraph = SimpleModuleGraph().compute(project)

        val betweennessCentrality = BetweennessCentrality(moduleGraph, false, THROW_EXCEPTION_ON_OVERFLOW).scores
        return Output(moduleGraph, betweennessCentrality)
    }

    public data class Output(
        val moduleGraph: SimpleDirectedGraph<Project, DefaultEdge>,
        val betweennessCentrality: Map<Project, Double>,
    )
}
