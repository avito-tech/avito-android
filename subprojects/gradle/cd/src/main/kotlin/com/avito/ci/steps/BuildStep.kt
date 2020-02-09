package com.avito.ci.steps

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

abstract class BuildStep(protected val context: String) {

    internal abstract fun registerTask(project: Project, rootTask: TaskProvider<out Task>)

}
