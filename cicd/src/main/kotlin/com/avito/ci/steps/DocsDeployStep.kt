package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.plugin.docsDeployTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

class DocsDeployStep(context: String) : BuildStep(context) {

    override val scope: Scope = Scope.ROOT_PROJECT

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        rootTask.dependsOn(project.tasks.docsDeployTask())
    }
}
