package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.plugin.docsCheckTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

class DocsCheckStep(context: String) : BuildStep(context) {

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        rootTask.dependsOn(project.tasks.docsCheckTask())
    }
}
