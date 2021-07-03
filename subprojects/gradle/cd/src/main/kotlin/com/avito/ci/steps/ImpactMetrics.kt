package com.avito.ci.steps

import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.namedOrNull
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

public class ImpactMetrics(context: String, name: String) : BuildStep(context, name) {

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        require(project.isRoot()) { failMessage("should be configured for root project") }

        val impactMetricsTask = requireNotNull(project.tasks.namedOrNull("impactMetrics")) {
            failMessage("impactMetrics task not registered for root project")
        }

        rootTask.configure { it.dependsOn(impactMetricsTask) }
    }

    private fun failMessage(reason: String): String = "Failed to setup ImpactMetrics: $reason"
}
