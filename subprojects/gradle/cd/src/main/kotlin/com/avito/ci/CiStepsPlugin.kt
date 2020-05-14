package com.avito.ci

import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.NamedDomainObjectContainer
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

        buildContainer.registerPredefinedBuildTasks()

        // TODO: MBS-6118. В CD во время конфигурации активно используется git. Это слишком долго.
        // Локально не создаем эти таски для ускорения конфигурации
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

    @Deprecated("remove when supported in Avito")
    private fun NamedDomainObjectContainer<BuildStepListExtension>.registerPredefinedBuildTasks() {
        register("localCheck") { buildTask ->
            buildTask.description.set("Fast local checks with impact analysis")
        }

        register("release") { buildTask ->
            buildTask.description.set("Task to build for release (runs full non-blocking full regression suite)")
        }

        register("uploadArtifacts") { buildTask ->
            buildTask.description.set("Task to upload artifacts without checks")
        }

        register("fullCheck") { buildTask ->
            buildTask.description.set("Task to run all specified check on project")
        }

        register("fastCheck") { buildTask ->
            buildTask.description.set("Task to run fast check of project. Based on impact analysis")
        }
    }
}
