package com.avito.ci.steps

import com.avito.ci.TasksPredicate
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

public class CustomTaskStep(context: String, name: String) : BuildStep(context, name) {

    public var tasksPredicate: TasksPredicate<*>? = null

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        val predicate = requireNotNull(tasksPredicate) {
            "Tasks predicate for $context.$name build step is not set"
        }
        rootTask.configure {
            it.dependsOn(
                predicate.find(project.tasks)
            )
        }
    }
}
