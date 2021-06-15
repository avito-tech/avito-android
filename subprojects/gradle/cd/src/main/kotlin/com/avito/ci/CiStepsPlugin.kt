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
            @Suppress("UnstableApiUsage")
            val explicitOverride = project.providers
                .gradleProperty(explicitBuildStepsOverrideProperty)
                .forUseAtConfigurationTime()
                .map { it.toBoolean() }
                .getOrElse(false)

            BuildStepListExtension(
                buildStepListName = name,
                explicitOverride,
                project.objects
            )
        }

        project.extensions.add("builds", buildContainer)

        if (project.buildEnvironment !is BuildEnvironment.CI) return

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

/**
 * Temporary feature toggle for a new behaviour. It breaks backward compatibility.
 * It will be deleted in next releases.
 */
internal const val explicitBuildStepsOverrideProperty = "com.avito.ci.build.step.explicitOverride"
