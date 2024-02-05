package com.avito.module.metrics

import com.avito.kotlin.dsl.isRoot
import com.avito.module.dependencies.graphbuilder.SimpleModuleGraph
import org.gradle.api.Project
import org.jgrapht.alg.scoring.BetweennessCentrality
import org.jgrapht.alg.scoring.BetweennessCentrality.OverflowStrategy.THROW_EXCEPTION_ON_OVERFLOW

public class CollectModuleBetweennessCentralityAction {

    public fun collect(project: Project): Map<Project, Double> {
        assert(project.isRoot())

        val graph = SimpleModuleGraph().compute(project)
        return BetweennessCentrality(graph, false, THROW_EXCEPTION_ON_OVERFLOW).scores
    }
}
