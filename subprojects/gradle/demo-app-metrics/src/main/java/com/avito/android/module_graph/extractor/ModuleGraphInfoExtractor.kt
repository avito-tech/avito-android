package com.avito.android.module_graph.extractor

import com.avito.android.module_graph.models.GradleDependency
import com.avito.android.module_graph.models.ModuleGraphEdge
import com.avito.android.module_graph.models.ModuleGraphInfo
import com.avito.android.module_graph.models.ModuleLinesOfCode
import com.avito.android.module_graph.models.ModulesGraph
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleType
import java.io.File

public class ModuleGraphInfoExtractor(
    private val dependencies: List<GradleDependency>,
    private val modulesToModuleTypes: Map<String, ModuleType>,
    private val linesOfCodeCounter: ModuleLinesOfCodeCounter,
    private val projectDir: File,
) {

    public fun extractInfo(): ModuleGraphInfo {
        val mainSourceSetDependencyTypes = setOf(
            GradleDependency.Type.Api,
            GradleDependency.Type.Implementation,
            GradleDependency.Type.CompileOnly,
            GradleDependency.Type.RuntimeOnly,
        )

        val moduleGraph = createGraph(dependencies)
        val moduleNames = moduleGraph.vertexSet().filter { isInternalModule(it) }.sorted()
        val applicationNames = moduleNames.filter { isDemoOrUserApp(it) }

        val modulesToLinesOfCode = moduleNames.associateWith { moduleName ->
            val isApplication = isDemoOrUserApp(moduleName)
            ModuleLinesOfCode(
                main = linesOfCodeCounter.count(projectDir, moduleName, "main"),
                test = if (isApplication) linesOfCodeCounter.count(projectDir, moduleName, "test") else null,
                androidTest = if (isApplication) {
                    linesOfCodeCounter.count(projectDir, moduleName, "androidTest")
                } else {
                    null
                },
            )
        }

        val modulesToDependencyModules = moduleNames.associateWith { moduleName ->
            val dependencyModules = mutableSetOf<String>()
            getDependencyModules(
                moduleGraph = moduleGraph,
                moduleName = moduleName,
                firstDependencyTypes = if (isDemoOrUserApp(moduleName)) {
                    GradleDependency.Type.entries
                } else {
                    mainSourceSetDependencyTypes
                },
                followingDependencyTypes = mainSourceSetDependencyTypes,
                result = dependencyModules,
            )
            dependencyModules
        }

        val modulesToSizeInLinesOfCode =
            modulesToDependencyModules.mapValues { (moduleName, moduleDependencies) ->
                var size = moduleDependencies.sumOf {
                    modulesToLinesOfCode.getValue(it).main
                }
                if (isDemoOrUserApp(moduleName)) {
                    val applicationLinesOfCode = modulesToLinesOfCode.getValue(moduleName)
                    size += applicationLinesOfCode.test.orZero() + applicationLinesOfCode.androidTest.orZero()
                }
                size
            }

        val modulesToImpactedApps = moduleNames.associateWith { moduleName ->
            modulesToDependencyModules.mapNotNull { (name, dependencies) ->
                if (isDemoOrUserApp(name) && moduleName in dependencies) name else null
            }
        }

        val internalModuleEdges = moduleGraph.edgeSet()
            .filter { (from, to) ->
                isInternalModule(from) && isInternalModule(to)
            }
            .map {
                ModuleGraphEdge(
                    from = it.from,
                    to = it.to,
                    type = it.type.name,
                )
            }

        return ModuleGraphInfo(
            dependencies = internalModuleEdges,
            applications = applicationNames,
            linesOfCode = modulesToLinesOfCode,
            transitiveLinesOfCode = modulesToSizeInLinesOfCode,
            impactedApplications = modulesToImpactedApps,
        )
    }

    private fun createGraph(dependencies: List<GradleDependency>): ModulesGraph {
        val graph = ModulesGraph(null, null, false)
        dependencies.forEach {
            graph.addVertex(it.from)
            graph.addVertex(it.to)
            graph.addEdge(it.from, it.to, it)
        }
        return graph
    }

    private fun getDependencyModules(
        moduleGraph: ModulesGraph,
        moduleName: String,
        firstDependencyTypes: Collection<GradleDependency.Type>,
        followingDependencyTypes: Collection<GradleDependency.Type>,
        result: MutableSet<String>,
    ) {
        if (moduleName in result || !isInternalModule(moduleName)) return
        result.add(moduleName)
        moduleGraph
            .outgoingEdgesOf(moduleName)
            .filter {
                it.type in firstDependencyTypes
            }
            .forEach {
                getDependencyModules(
                    moduleGraph,
                    it.to,
                    followingDependencyTypes,
                    followingDependencyTypes,
                    result,
                )
            }
    }

    private fun isInternalModule(moduleName: String): Boolean {
        return moduleName.startsWith(":")
    }

    private fun isDemoOrUserApp(moduleName: String): Boolean {
        val type = modulesToModuleTypes[moduleName]?.type
        return type in listOf(FunctionalType.UserApp, FunctionalType.DemoApp)
    }
}

private fun Int?.orZero(): Int = this ?: 0
