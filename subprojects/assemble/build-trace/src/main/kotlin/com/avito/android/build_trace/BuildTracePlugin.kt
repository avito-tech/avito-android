package com.avito.android.build_trace

import com.avito.android.build_trace.internal.BuildTraceListener
import com.avito.android.critical_path.CriticalPathRegistry
import com.avito.android.gradle.metric.GradleCollector
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project

public abstract class BuildTracePlugin : Plugin<Project> {

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

        val buildTraceListener = BuildTraceListener(
            output = outputDir.file("build.trace").get().asFile,
            logger = project.logger
        )
        CriticalPathRegistry.addListener(project, buildTraceListener)

        GradleCollector.initialize(project, listOf(buildTraceListener))
    }
}

internal const val extensionName = "buildTrace"
