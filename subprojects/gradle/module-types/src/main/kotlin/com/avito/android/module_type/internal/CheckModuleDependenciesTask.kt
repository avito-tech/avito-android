package com.avito.android.module_type.internal

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.android.module_type.DependencyRestriction
import com.avito.android.module_type.ModuleTypeRootExtension
import com.avito.android.module_type.ModuleWithType
import com.avito.android.module_type.Severity
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
    @get:Optional
    abstract val severity: Property<Severity>

    @get:Input
    abstract val dependentProjects: SetProperty<String>

    @get:Input
    abstract val restrictions: ListProperty<DependencyRestriction>

    @TaskAction
    fun checkDependencies() {
        val violations = ModulesRestrictionsFinder(readModules(), restrictions.get()).violations()

        if (violations.isNotEmpty()) {
            val error = buildProblem(violations).asRuntimeException()

            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (severity.getOrElse(Severity.fail)) {
                Severity.fail -> throw error
                Severity.warning -> logger.warn("forbidden dependencies", error)
                Severity.ignore -> {
                    // no-op
                }
            }
        }
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
                appendLine()
                appendLine("${violation.module.description()} depends on ${violation.dependency.description()}")
                appendLine("It violates a constraint: ${violation.restriction.matcher.description()}")
            }
        }
        @Suppress("MaxLineLength")
        return Problem.Builder(
            shortDescription = "Found forbidden dependencies between modules",
            context = "In modules: ${modulesWithErrors.joinToString()}"
        )
            .because(errorsDescription)
            .addSolution("Get rid of wrong dependencies")
            .addSolution("Add module to ${DependencyRestriction::class.java.simpleName}.${DependencyRestriction::exclusions.name}")
            .addSolution("Disable checks in ${ModuleTypeRootExtension.name}.${ModuleTypeRootExtension::severity.name}")
            .build()
    }

    private fun ModuleWithType.description(): String {
        return if (type == null) {
            "module $path"
        } else {
            "module $path (${type.description()})"
        }
    }

    companion object {
        internal const val name: String = "checkModulesDependencies"
    }
}
