package com.avito.ci.steps

import com.avito.android.withAndroidModule
import com.avito.impact.configuration.internalModule
import com.avito.kotlin.dsl.withType
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.AbstractCompile

public class CompileUiTests(context: String, name: String) : SuppressibleBuildStep(context, name),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        if (useImpactAnalysis && !project.internalModule.isModified()) return

        project.withAndroidModule {
            project.tasks.withType<AbstractCompile>().forEach { compileTask ->
                val isForDebugBuild = compileTask.name.contains("debug", ignoreCase = true)
                if (isForDebugBuild) {
                    rootTask.configure { it.dependsOn(compileTask) }
                }
            }
        }
    }
}
