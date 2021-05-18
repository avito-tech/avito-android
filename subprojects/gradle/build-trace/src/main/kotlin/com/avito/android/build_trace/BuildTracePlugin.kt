package com.avito.android.build_trace

import com.avito.android.build_trace.internal.BuildTraceListener
import com.avito.android.build_trace.internal.critical_path.CriticalPathListener
import com.avito.android.build_trace.internal.critical_path.CriticalPathSerialization
import com.avito.android.gradle.metric.GradleCollector
import com.avito.kotlin.dsl.isRoot
import com.avito.logger.GradleLoggerFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty

open class BuildTracePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        check(project.isRoot()) {
            "Plugin must be applied to the root project but was applied to ${project.path}"
        }
        val extension = project.extensions.create(extensionName, BuildTraceExtension::class.java)

        project.afterEvaluate {
            registerListeners(project, extension)
        }
    }

    private fun registerListeners(project: Project, extension: BuildTraceExtension) {
        val isEnabled = extension.enabled.getOrElse(false)
        if (!isEnabled) return

        val outputDir = extension.output.convention(
            project.layout.buildDirectory.dir("reports/build-trace")
        )

        val loggerFactory = GradleLoggerFactory.fromPlugin(this, project)

        val criticalPathListener = criticalPathListener(outputDir, loggerFactory)

        val buildTraceListener = BuildTraceListener(
            output = outputDir.file("build.trace").get().asFile,
            criticalPathProvider = criticalPathListener,
            loggerFactory = loggerFactory
        )
        GradleCollector.initialize(project, listOf(criticalPathListener, buildTraceListener))
    }

    private fun criticalPathListener(
        output: DirectoryProperty,
        loggerFactory: GradleLoggerFactory
    ): CriticalPathListener {
        val writer = CriticalPathSerialization(
            report = output.file("critical_path.json").get().asFile,
        )
        return CriticalPathListener(writer, loggerFactory)
    }
}

internal const val extensionName = "buildTrace"
