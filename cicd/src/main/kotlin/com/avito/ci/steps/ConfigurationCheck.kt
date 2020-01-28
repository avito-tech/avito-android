package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.impact.ModifiedProjectsFinder
import com.avito.utils.logging.ciLogger
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

class ConfigurationCheck(context: String) : SuppressibleBuildStep(context),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    override val scope: Scope = Scope.ROOT_PROJECT

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        if (suppressFailures) return

        // TODO: configure externally
        val testsModule = project.rootProject.findProject("build-script-test")
        if (testsModule == null) {
            project.ciLogger.debug("Project configuration tests not found")
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
