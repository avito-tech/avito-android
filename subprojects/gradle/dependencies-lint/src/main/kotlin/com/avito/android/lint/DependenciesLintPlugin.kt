package com.avito.android.lint

import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

open class DependenciesLintPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        check(project.isRoot()) { "Plugin must be applied to the root project, but was applied to ${project.path}" }

        val task = registerTask(project)
        configure(project, task)
    }

    private fun registerTask(project: Project): TaskProvider<out Task> {
        return project.tasks.register<DependenciesLintTask>("lintDependencies") {
            outputs.upToDateWhen { false }
        }
    }

    private fun configure(project: Project, task: TaskProvider<out Task>) {
        fun addDependencyToCompile(module: Project) {
            task.configure {
                it.dependsOn(module.tasks.withType(KotlinCompile::class.java))
            }
        }

        project.subprojects.forEach { subProject ->
            subProject.pluginManager.withPlugin("com.android.library") {
                addDependencyToCompile(subProject)
            }
            subProject.pluginManager.withPlugin("com.android.application") {
                addDependencyToCompile(subProject)
            }
            subProject.pluginManager.withPlugin("kotlin") {
                addDependencyToCompile(subProject)
            }
        }
    }
}
