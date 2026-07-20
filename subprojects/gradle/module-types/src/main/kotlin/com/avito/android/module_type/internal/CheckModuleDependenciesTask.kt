@file:Suppress("DEPRECATION")

package com.avito.android.module_type.internal

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.Severity
import com.avito.android.module_type.restrictions.extension.BetweenDifferentAppsRestrictionExtension
import com.avito.android.module_type.restrictions.extension.BetweenFunctionalTypesExtension
import com.avito.android.module_type.restrictions.extension.ToWiringRestrictionExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

internal abstract class CheckModuleDependenciesTask @Inject constructor(
    projectLayout: ProjectLayout,
) : DefaultTask() {

    @get:Internal
    val rootDir: Directory = projectLayout.projectDirectory

    @get:Input
    abstract val dependentProjects: SetProperty<String>

    @get:Input
    abstract val betweenFunctionalTypesRestrictions: ListProperty<BetweenFunctionalTypesExtension>

    @get:Input
    @get:Optional
    abstract val betweenDifferentAppsRestriction: Property<BetweenDifferentAppsRestrictionExtension>

    @get:Input
    @get:Optional
    abstract val toWiringRestriction: Property<ToWiringRestrictionExtension>

    @get:Internal
    abstract val solutionMessage: Property<String>

    @TaskAction
    fun checkDependencies() {
        val restrictions: List<com.avito.android.module_type.restrictions.DependencyRestriction> = buildList {
            addAll(betweenFunctionalTypesRestrictions.getOrElse(emptyList()).map { it.getRestriction() })
            if (betweenDifferentAppsRestriction.isPresent) {
                add(betweenDifferentAppsRestriction.get().getRestriction())
            }
            if (toWiringRestriction.isPresent) {
                add(toWiringRestriction.get().getRestriction())
            }
        }
        val modules = readModules()
        val violations = ModulesRestrictionsFinder(modules, restrictions).violations()

        if (violations.isNotEmpty()) {
            throw buildProblem(violations).asRuntimeException()
        }

        checkUnusedSharedBetweenAppsFlags(modules)
    }

    private fun checkUnusedSharedBetweenAppsFlags(modules: Set<ModuleDescription>) {
        if (!betweenDifferentAppsRestriction.isPresent) return
        val extension = betweenDifferentAppsRestriction.get()
        if (extension.severity.get() != Severity.fail) return

        val unusedFlags = UnusedSharedBetweenAppsFinder(
            moduleDescriptions = modules,
            commonApp = extension.commonApp.get(),
            sharingApps = extension.sharingApps.get(),
        ).findUnusedFlags()

        if (!unusedFlags.isEmpty) {
            throw buildUnusedFlagsProblem(unusedFlags)
                .asRuntimeException()
        }
    }

    private fun buildUnusedFlagsProblem(unusedFlags: UnusedSharedBetweenAppsFinder.UnusedFlags): Problem {
        val allModules = unusedFlags.unjustifiedModules + unusedFlags.redundantCommonModules
        val description = buildString {
            if (unusedFlags.unjustifiedModules.isNotEmpty()) {
                appendLine(
                    "These modules are marked with sharedBetweenApps, but no module of another " +
                        "application depends on them directly:"
                )
                unusedFlags.unjustifiedModules.forEach { appendLine("  - $it") }
            }
            if (unusedFlags.redundantCommonModules.isNotEmpty()) {
                appendLine(
                    "These common modules are marked with sharedBetweenApps, but the flag is redundant: " +
                        "common modules are available to every application without it:"
                )
                unusedFlags.redundantCommonModules.forEach { appendLine("  - $it") }
            }
        }
        return Problem.Builder(
            shortDescription = "Found unused sharedBetweenApps flags",
            context = "In modules: ${allModules.joinToString()}"
        )
            .because(description)
            .addSolution("Remove 'sharedBetweenApps.set(true)' from the module's build script")
            .addSolution(solutionMessage.getOrElse("No solution help message"))
            .build()
    }

    private fun readModules(): Set<ModuleDescription> {
        val reader = ModuleDescriptionReader(rootDir.asFile)
        return dependentProjects.get()
            .map { reader.read(it) }
            .toSet()
    }

    private fun buildProblem(violations: List<RestrictionViolation>): Problem {
        val modulesWithErrors = violations.map { it.module.path }.toSet()

        val errorsDescription = buildString {
            violations.forEach { violation ->
                with(violation) {
                    appendLine()
                    appendLine("${module.description()} depends on ${dependency.description()} in $configurationType")
                    appendLine("It violates a constraint: ${restriction.reason}")
                }
            }
        }
        return Problem.Builder(
            shortDescription = "Found forbidden dependencies between modules",
            context = "In modules: ${modulesWithErrors.joinToString()}"
        )
            .because(errorsDescription)
            .addSolution(solutionMessage.getOrElse("No solution help message"))
            .build()
    }

    private fun ModuleWithType.description(): String {
        return "module $path (${type.description()})"
    }

    companion object {
        internal const val name: String = "checkModulesDependencies"
    }
}
