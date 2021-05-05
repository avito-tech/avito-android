package com.avito.android.build_trace

import com.avito.android.build_trace.internal.BuildTraceListener
import com.avito.android.build_trace.internal.CriticalPathListener
import com.avito.android.gradle.metric.GradleCollector
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.logger.GradleLoggerFactory
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Plugin
import org.gradle.api.Project

open class BuildTracePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        check(project.isRoot()) {
            "Plugin must be applied to the root project but was applied to ${project.path}"
        }
        if (isBuildTraceEnabled(project)) {
            val loggerFactory = GradleLoggerFactory.fromPlugin(this, project)

            val criticalPath = CriticalPathListener.from(project)
            val buildTrace = BuildTraceListener.from(project, loggerFactory)

            GradleCollector.initialize(project, listOf(criticalPath, buildTrace))
        }
    }

    // TODO: enable by a project extension or a property
    private fun isBuildTraceEnabled(project: Project): Boolean {
        return project.buildEnvironment is BuildEnvironment.CI
            || project.gradle.startParameter.isBuildScan
            || project.gradle.startParameter.isProfile
            || project.getBooleanProperty("android.enableProfileJson", default = false)
    }
}
