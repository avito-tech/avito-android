package com.avito.ci

import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.register

class CiStepsPlugin : Plugin<Project> {

    private val taskGroup = "ci"

    override fun apply(project: Project) {

        val buildContainer = project.container { name ->
            BuildStepListExtension(name, project.objects)
        }

        project.extensions.add("builds", buildContainer)

        if (project.buildEnvironment !is BuildEnvironment.CI) return

        buildContainer.all { buildTask ->

            val task = project.tasks.register<Task>(buildTask.name) {
                group = taskGroup
                description = buildTask.description.get()
            }

            project.gradle.projectsEvaluated {
                buildTask.steps.get().forEach { step ->
                    step.registerTask(project, task)
                }
            }
        }
    }
}
