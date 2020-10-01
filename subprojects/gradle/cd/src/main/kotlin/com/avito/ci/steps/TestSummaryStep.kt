package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.impact.configuration.internalModule
import com.avito.instrumentation.extractReportCoordinates
import com.avito.instrumentation.instrumentationTask
import com.avito.test.summary.flakyReportTask
import com.avito.test.summary.testSummaryPluginId
import com.avito.test.summary.testSummaryTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

class TestSummaryStep(context: String, name: String) : SuppressibleBuildStep(context, name),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    var configuration: String = ""

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        require(project.pluginManager.hasPlugin(testSummaryPluginId)) {
            "TestSummary step can't be initialized without $testSummaryPluginId plugin applied"
        }

        require(configuration.isNotBlank()) {
            "TestSummary step can't be initialized without provided instrumentation configuration"
        }

        if (useImpactAnalysis && !project.internalModule.isModified()) return

        val instrumentationTask = project.tasks.instrumentationTask(configuration)

        val testSummaryTask = project.tasks.testSummaryTask()
        testSummaryTask.configure {
            it.dependsOn(instrumentationTask)
            it.reportCoordinates.set(instrumentationTask.extractReportCoordinates())
        }
        rootTask.dependsOn(testSummaryTask)

        val flakyReportTask = project.tasks.flakyReportTask()
        flakyReportTask.configure {
            it.dependsOn(instrumentationTask)
            it.reportCoordinates.set(instrumentationTask.extractReportCoordinates())
        }
        rootTask.dependsOn(flakyReportTask)
    }
}
