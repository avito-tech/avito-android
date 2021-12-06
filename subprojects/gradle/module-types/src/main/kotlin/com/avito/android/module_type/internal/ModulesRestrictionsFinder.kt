package com.avito.android.module_type.internal

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.android.module_type.DependencyRestriction
import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.pluginId
import com.avito.module.configurations.ConfigurationType

internal class ModulesRestrictionsFinder(
    modules: Set<ModuleDescription>,
    private val restrictions: List<DependencyRestriction>
) {

    private val modules: Set<ResolvedModuleDescription> = resolveReferencesToModules(modules)

    private data class ResolvedModuleDescription(
        val module: ModuleWithType,
        val directDependencies: Map<ConfigurationType, Set<ModuleWithType>>
    )

    fun violations(): List<RestrictionViolation> {
        return modules.flatMap { moduleDescription ->
            violations(moduleDescription)
        }
    }

    private fun violations(moduleDescription: ResolvedModuleDescription): List<RestrictionViolation> {
        val violations = mutableListOf<RestrictionViolation>()

        restrictions.forEach { restriction ->
            moduleDescription.directDependencies
                .forEach { (configuration, dependentModules) ->
                    dependentModules.forEach { dependentModule ->
                        if (restriction.isViolated(moduleDescription.module, dependentModule, configuration)) {
                            violations.add(
                                RestrictionViolation(
                                    module = moduleDescription.module,
                                    dependency = dependentModule,
                                    restriction = restriction
                                )
                            )
                        }
                    }
                }
        }
        return violations
    }

    private fun resolveReferencesToModules(modules: Set<ModuleDescription>): Set<ResolvedModuleDescription> {
        val pathToDescription = modules
            .map { it.module.path to it }
            .toMap()

        return modules.map { description ->
            val moduleDependencies = description.directDependencies
                .mapValues {
                    it.value.map { path ->
                        val moduleDescription = pathToDescription[path] ?: throwNotFoundModule(path)
                        moduleDescription.module
                    }.toSet()
                }

            ResolvedModuleDescription(
                module = description.module,
                directDependencies = moduleDependencies
            )
        }.toSet()
    }

    private fun throwNotFoundModule(module: String): Nothing {
        throw Problem.Builder(
            shortDescription = "Not found module description for $module",
            context = "Module types plugin resolve metadata for dependant modules"
        )
            .addSolution("Check that plugin $pluginId is applied to $module")
            .build()
            .asRuntimeException()
    }
}

internal class RestrictionViolation(
    val module: ModuleWithType,
    val dependency: ModuleWithType,
    val restriction: DependencyRestriction
)
