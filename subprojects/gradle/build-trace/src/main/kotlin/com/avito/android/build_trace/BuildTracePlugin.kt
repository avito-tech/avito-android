package com.avito.android.build_trace

import com.avito.android.build_trace.internal.BuildTraceListener
import com.avito.android.build_trace.internal.critical_path.CriticalPathListener
import com.avito.android.build_trace.internal.critical_path.CriticalPathSerialization
import com.avito.android.gradle.metric.GradleCollector
import com.avito.kotlin.dsl.isRoot
import com.avito.logger.GradleLoggerFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

open class BuildTracePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        check(project.isRoot()) {
            "Plugin must be applied to the root project but was applied to ${project.path}"
        }
        val extension = project.extensions.create("buildTrace", BuildTraceExtension::class.java)

        project.afterEvaluate {
            registerListeners(project, extension)
        }
    }

    private fun registerListeners(project: Project, extension: BuildTraceExtension) {
        val isEnabled = extension.enabled.getOrElse(false)
        if (!isEnabled) return

        val loggerFactory = GradleLoggerFactory.fromPlugin(this, project)

        val criticalPathListener = criticalPathListener(project)

        val buildTraceListener = BuildTraceListener(
            output = File(pluginOutputDir(project), "build.trace"),
            criticalPathProvider = criticalPathListener,
            loggerFactory = loggerFactory
        )
        GradleCollector.initialize(project, listOf(criticalPathListener, buildTraceListener))
    }

    private fun criticalPathListener(project: Project): CriticalPathListener {
        val writer = CriticalPathSerialization(
            report = File(pluginOutputDir(project), "critical_path.json")
        )
        return CriticalPathListener(writer)
    }

    private fun pluginOutputDir(project: Project): File =
        File(project.projectDir, "outputs/build-trace")
}
