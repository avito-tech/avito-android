package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.impact.ModifiedProjectsFinder
import com.avito.logger.GradleLoggerFactory
import com.avito.logger.create
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

class ConfigurationCheck(context: String, name: String) : SuppressibleBuildStep(context, name),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        if (suppressFailures) return // TODO: run anyway, but don't fail

        val logger = GradleLoggerFactory.fromProject(project).create<ConfigurationCheck>()

        // TODO: configure externally
        val testsModule = project.rootProject.findProject("build-script-test")
        if (testsModule == null) {
            logger.debug("Project configuration tests not found")
            return
        }
        testsModule.plugins.withId("kotlin") {
            val tests = testsModule.tasks.named("test")

            tests.configure { testTask ->
                testTask.onlyIf { !useImpactAnalysis || hasAnyChanges(project) }
            }

            rootTask.dependsOn(tests)
        }
    }

    private fun hasAnyChanges(project: Project): Boolean {
        return ModifiedProjectsFinder.from(project).findModifiedProjects().isNotEmpty()
    }
}
