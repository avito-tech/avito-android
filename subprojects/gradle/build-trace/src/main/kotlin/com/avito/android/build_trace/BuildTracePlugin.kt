package com.avito.android.build_trace

import com.avito.android.gradle.metric.BuildEventsListener
import com.avito.android.gradle.metric.GradleCollector
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.logger.GradleLoggerFactory
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

open class BuildTracePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        check(project.isRoot()) {
            "Plugin must be applied to the root project but was applied to ${project.path}"
        }
        if (isBuildTraceEnabled(project)) {
            GradleCollector.initialize(project, listOf(buildTraceConsumer(project)))
        }
    }

    private fun buildTraceConsumer(project: Project): BuildEventsListener = BuildTraceConsumer(
        // TODO: pass it from an extension
        output = File(project.projectDir, "outputs/trace/build.trace"),
        loggerFactory = GradleLoggerFactory.fromPlugin(this, project)
    )

    // TODO: enable by a project extension
    private fun isBuildTraceEnabled(project: Project): Boolean {
        return project.buildEnvironment is BuildEnvironment.CI
            || project.gradle.startParameter.isBuildScan
            || project.gradle.startParameter.isProfile
            || project.getBooleanProperty("android.enableProfileJson", default = false)
    }
}
