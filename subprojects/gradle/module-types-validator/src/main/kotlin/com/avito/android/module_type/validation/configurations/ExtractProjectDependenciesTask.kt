package com.avito.android.module_type.validation.configurations

import com.avito.android.Result
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.artifacts.result.UnresolvedDependencyResult
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

internal const val REPORT_INDENT: String = "    "

@DisableCachingByDefault(because = "Only dependency metadata is rendered; artifact contents are not inputs")
public abstract class ExtractProjectDependenciesTask : DefaultTask() {

    @get:Input
    public abstract val roots: MapProperty<String, ResolvedComponentResult>

    @get:OutputFile
    public abstract val output: RegularFileProperty

    @TaskAction
    public fun extract() {
        val roots = roots.get()
        check(roots.isNotEmpty()) { "No JVM or Android classpath configurations are available for $path" }
        val report = roots.entries.joinToString(separator = "\n\n") { (configurationName, root) ->
            renderProjectDependencies(configurationName, root).getOrThrow()
        }
        output.get().asFile.writeText(report)
    }

    private fun renderProjectDependencies(configurationName: String, root: ResolvedComponentResult): Result<String> {
        val buildPath = (root.id as ProjectComponentIdentifier).build.buildPath
        val expanded = HashSet<ComponentIdentifier>()
        val report = StringBuilder(configurationName)
        fun visit(component: ResolvedComponentResult, depth: Int): Result<Unit> {
            component.dependencies.forEach { dependency ->
                if (dependency is UnresolvedDependencyResult) {
                    return Result.Failure(
                        GradleException(
                            "Cannot resolve dependencies for configuration '$configurationName' in $path: " +
                                "${dependency.attempted.displayName} requested by ${component.id.displayName}",
                            dependency.failure,
                        )
                    )
                }
                if (dependency !is ResolvedDependencyResult || dependency.isConstraint) return@forEach
                val selected = dependency.selected
                val projectId = (selected.id as? ProjectComponentIdentifier)
                    ?.takeIf { it.build.buildPath == buildPath }
                val childDepth = if (projectId != null) {
                    report.append('\n').append(REPORT_INDENT.repeat(depth)).append(projectId.projectPath)
                    depth + 1
                } else {
                    depth
                }
                if (expanded.add(selected.id)) {
                    visit(selected, childDepth).getOrElse { return Result.Failure(it) }
                }
            }
            return Result.Success(Unit)
        }
        return visit(root, 0).map { report.toString() }
    }
}
