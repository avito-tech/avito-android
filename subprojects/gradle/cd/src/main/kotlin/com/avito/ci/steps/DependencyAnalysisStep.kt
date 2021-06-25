package com.avito.ci.steps

import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

/**
 * https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin
 *
 * Why this step exists when we have `buildHealth` task?
 *  `buildHealth` has a noisy output that is hard to read:
 *
 *      - Modules without errors: https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin/pull/402
 *      - Ignored redundantPlugins: https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin/issues/431
 *
 *  Per module reports helps to point out at least failed module.
 *
 * Why don't we use impact analysis here?
 *  `projectHealth` task depends on tree of tasks from dependency-analysis plugins that depend on AGP and Kotlin tasks.
 *   It's not clear how to break this chain correctly in execution phase.
 */
class DependencyAnalysisStep(context: String, name: String) : BuildStep(context, name) {

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        require(project.isRoot()) {
            "step must be configured for the root project"
        }
        project.subprojects { subproject ->
            subproject.plugins.withId("com.autonomousapps.dependency-analysis") {

                val projectHealthTask = subproject.tasks.named("projectHealth")

                rootTask.configure {
                    it.dependsOn(projectHealthTask)
                }
            }
        }
    }
}
