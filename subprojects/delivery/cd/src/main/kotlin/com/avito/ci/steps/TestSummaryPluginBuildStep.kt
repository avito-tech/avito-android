package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.stats.statsdConfig
import com.avito.impact.configuration.internalModule
import com.avito.instrumentation.extractReportCoordinates
import com.avito.instrumentation.instrumentationTaskDefaultEnvironment
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.summary.TestSummaryExtension
import com.avito.test.summary.TestSummaryFactory
import com.avito.test.summary.testSummaryPluginId
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType

public abstract class TestSummaryPluginBuildStep(context: String, name: String) :
    BuildStep(context, name),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    public var configuration: String = ""

    protected abstract val stepName: String

    protected abstract fun getOrCreateTask(
        rootTasksContainer: TaskContainer,
        extension: TestSummaryExtension,
        reportCoordinates: ReportCoordinates,
        testSummaryFactory: TestSummaryFactory,
    ): TaskProvider<out Task>

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        require(project.rootProject.pluginManager.hasPlugin(testSummaryPluginId)) {
            "$stepName step can't be initialized without $testSummaryPluginId plugin applied to root project"
        }

        require(configuration.isNotBlank()) {
            "$stepName step can't be initialized without provided instrumentation configuration"
        }

        if (useImpactAnalysis && !project.internalModule.isModified()) return

        // flavors not supported here
        val instrumentationTask = project.tasks.instrumentationTaskDefaultEnvironment(configuration, null)

        val reportCoordinates = instrumentationTask.extractReportCoordinates()

        val task = getOrCreateTask(
            rootTasksContainer = project.rootProject.tasks,
            extension = project.rootProject.extensions.getByType(),
            reportCoordinates = reportCoordinates.get(),
            testSummaryFactory = TestSummaryFactory(
                statsDConfig = project.statsdConfig,
            )
        )

        task.configure {
            it.dependsOn(instrumentationTask)
        }

        rootTask.dependsOn(task)
    }
}
