package com.avito.android.critical_path

import com.avito.android.critical_path.internal.CriticalPathReport
import com.avito.android.critical_path.internal.CriticalPathWriter
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project

public abstract class CriticalPathPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        check(project.isRoot()) {
            "Plugin must be applied to the root project but was applied to ${project.path}"
        }
        val extension = project.extensions.create(extensionName, CriticalPathExtension::class.java)

        project.afterEvaluate {
            registerListeners(project, extension)
        }
    }

    private fun registerListeners(project: Project, extension: CriticalPathExtension) {
        val isEnabled = extension.enabled.getOrElse(false)
        if (!isEnabled) return

        val reportWriter = createReportWriter(project, extension)

        CriticalPathRegistry.addListener(project, reportWriter)
    }

    private fun createReportWriter(project: Project, extension: CriticalPathExtension): CriticalPathWriter {
        val outputDir = extension.output.convention(
            project.layout.buildDirectory.dir("reports/critical-path")
        )
        val report = CriticalPathReport(
            report = outputDir.file("critical_path.json").get().asFile,
        )
        return CriticalPathWriter(report)
    }
}

internal const val extensionName = "criticalPath"
