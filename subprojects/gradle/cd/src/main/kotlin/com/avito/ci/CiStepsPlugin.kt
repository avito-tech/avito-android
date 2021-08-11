package com.avito.ci

import com.avito.logger.GradleLoggerFactory
import com.avito.logger.create
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.register

public class CiStepsPlugin : Plugin<Project> {

    private val taskGroup = "ci"

    override fun apply(project: Project) {
        val loggerFactory = GradleLoggerFactory.fromPlugin(this, project)
        val logger = loggerFactory.create<CiStepsPlugin>()
        val buildContainer = project.container { name ->
            BuildStepListExtension(
                buildStepListName = name,
                project.objects
            )
        }

        project.extensions.add("builds", buildContainer)

        if (project.buildEnvironment !is BuildEnvironment.CI) {
            logger.warn("The plugin is applied but it's disabled. Add -Pci=true to enable the plugin")
            return
        }

        buildContainer.all { buildTask ->

            val task = project.tasks.register<Task>(buildTask.buildStepListName) {
                group = taskGroup
                description = buildTask.taskDescription.orNull
            }

            project.gradle.projectsEvaluated {
                buildTask.steps.all { step ->
                    step.registerTask(project, task)
                }
            }
        }
    }
}
