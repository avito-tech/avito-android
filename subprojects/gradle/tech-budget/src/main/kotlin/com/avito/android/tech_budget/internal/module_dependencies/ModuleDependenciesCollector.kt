package com.avito.android.tech_budget.internal.module_dependencies

import com.avito.android.tech_budget.internal.module_dependencies.models.ModuleDependencies
import com.avito.module.configurations.ConfigurationType
import com.avito.module.dependencies.graphbuilder.AndroidAppsGraphBuilder
import com.avito.module.dependencies.graphbuilder.DependenciesGraphBuilder
import com.avito.module.dependencies.graphbuilder.ProjectConfigurationNode
import org.gradle.api.Project

internal object ModuleDependenciesCollector {

    internal fun collect(rootProject: Project): List<ModuleDependencies> {
        val graphBuilder = AndroidAppsGraphBuilder(DependenciesGraphBuilder(rootProject))
        val dependentModulesMap = mutableMapOf<ModulePath, Int>()
        val importedModulesMap = mutableMapOf<ModulePath, Int>()

        val appNodes: Set<ProjectConfigurationNode> = graphBuilder.buildDependenciesGraph(ConfigurationType.Main)
        for (appNode in appNodes) {
            collectModule(appNode, dependentModulesMap, importedModulesMap)
        }

        return importedModulesMap.map { (modulePath, importedModulesCount) ->
            ModuleDependencies(
                modulePath = modulePath.stringPath,
                directImportedModulesCount = importedModulesCount,
                directDependentModulesCount = dependentModulesMap.getOrDefault(modulePath, 0),
            )
        }
    }

    private fun collectModule(
        rootNode: ProjectConfigurationNode,
        dependentModulesMap: MutableMap<ModulePath, Int>,
        importedModulesMap: MutableMap<ModulePath, Int>
    ) {
        val rootModulePath: ModulePath = rootNode.modulePath
        if (importedModulesMap.containsKey(rootModulePath)) {
            // module was already visited
            return
        }
        val directDependencies = rootNode.directDependencies()
        importedModulesMap[rootModulePath] = directDependencies.count()

        directDependencies.forEach { dependency ->
            val path: ModulePath = dependency.modulePath
            dependentModulesMap[path] = dependentModulesMap.getOrDefault(path, 0) + 1

            collectModule(dependency, dependentModulesMap, importedModulesMap)
        }
    }
}

@JvmInline
private value class ModulePath(val stringPath: String)

private val ProjectConfigurationNode.modulePath: ModulePath
    get() = ModulePath(project.path)
