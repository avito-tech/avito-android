package com.avito.android.build_checks.internal

import com.avito.kotlin.dsl.namedOrNull
import org.gradle.StartParameter
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

internal class RootTaskCreator(
    private val project: Project
) {

    fun getOrCreate(): TaskProvider<Task> {
        val rootProject = project.rootProject

        if (!project.gradle.startParameter.taskNames.contains(":$rootTaskName")) {
            project.gradle.startParameter.addTaskNames(":$rootTaskName")
        }

        return rootProject.tasks.namedOrNull(rootTaskName)
            ?: rootProject.tasks.register(rootTaskName) {
                it.group = "verification"
                it.description = "Check typical build problems"
            }
    }

    private fun StartParameter.addTaskNames(vararg names: String) {
        // getter returns defensive copy
        setTaskNames(taskNames + names.toList())
    }
}

private const val rootTaskName = "checkBuildEnvironment"
