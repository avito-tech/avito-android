package com.avito.android.lint

import com.avito.android.lint.dependency.DependencyResolver
import com.avito.android.lint.dependency.SuspiciousDependency
import com.avito.android.lint.report.DependenciesReport
import com.avito.android.lint.report.LintIssue
import com.avito.android.lint.report.RedundantDependency
import com.avito.android.lint.report.ReportXmlAdapter
import com.avito.android.lint.report.Severity
import com.avito.android.lint.report.UnusedDependency
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class DependenciesLintTask : DefaultTask() {

    @OutputFile
    val reportFile: File = File(project.buildDir, "reports/dependencies-lint.xml")

    @TaskAction
    fun action() {
        val issues = mutableListOf<LintIssue>()
        project.subprojects
            .filter { it.hasSupportedPlugins() }
            .forEach { subProject ->
                issues.addAll(resolveIssues(subProject))
            }
        val report = DependenciesReport(issues)
        ReportXmlAdapter().write(report, reportFile)
    }

    private fun resolveIssues(module: Project): Collection<LintIssue> {
        module.logger.info("Analyzing dependencies of ${project.path}")
        val resolver = DependencyResolver(module)
        val component = resolver.suspiciousDependencies()
        return component.map { createIssue(module, it) }
    }

    private fun createIssue(module: Project, dependency: SuspiciousDependency): LintIssue {
        return when (dependency) {
            is SuspiciousDependency.Unused -> unusedDependency(module, dependency)
            is SuspiciousDependency.UsedTransitively -> redundantDependency(module, dependency)
        }
    }

    private fun unusedDependency(module: Project, dependency: SuspiciousDependency): UnusedDependency {
        val message = issueMessage(module, dependency)
        val summary = "No class usages found"
        return UnusedDependency(Severity.warning, message, summary)
    }

    private fun redundantDependency(module: Project, dependency: SuspiciousDependency.UsedTransitively): RedundantDependency {
        val message = issueMessage(module, dependency)
        val summary = "Uses transitive dependencies: " +
            dependency.transitiveComponents.joinToString { it.displayName }
        return RedundantDependency(Severity.warning, message, summary)
    }

    private fun issueMessage(module: Project, dependency: SuspiciousDependency): String {
        return "Unused dependency ${dependency.component.displayName} in the project ${module.path}"
    }

    private fun Project.hasSupportedPlugins(): Boolean {
        return plugins.hasPlugin("com.android.library")
            || plugins.hasPlugin("com.android.application")
            || plugins.hasPlugin("kotlin")
    }

}
