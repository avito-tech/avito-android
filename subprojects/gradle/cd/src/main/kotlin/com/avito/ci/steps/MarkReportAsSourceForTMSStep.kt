package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.impact.configuration.internalModule
import com.avito.instrumentation.instrumentationTask
import com.avito.plugin.tmsPluginId
import com.avito.plugin.markReportAsSourceTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

class MarkReportAsSourceForTMSStep(context: String, name: String) : BuildStep(context, name),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    var configuration: String = ""

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        require(project.pluginManager.hasPlugin(tmsPluginId)) {
            "MarkReportAsSourceForTMSStep can't be initialized without $tmsPluginId plugin applied"
        }
        if (useImpactAnalysis && !project.internalModule.isModified()) return

        val instrumentationTask = project.tasks.instrumentationTask(configuration)

        val markReportAsSourceTask = project.tasks.markReportAsSourceTask()

        markReportAsSourceTask.configure {
            it.dependsOn(instrumentationTask)

            @Suppress("UnstableApiUsage")
            it.reportCoordinates.set(instrumentationTask.flatMap { task ->
                task.instrumentationConfiguration.map { config ->
                    config.instrumentationParams.reportCoordinates()
                }
            })
        }

        rootTask.dependsOn(markReportAsSourceTask)
    }
}
