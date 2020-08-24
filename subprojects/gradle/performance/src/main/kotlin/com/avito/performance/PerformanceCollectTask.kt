package com.avito.performance

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class PerformanceCollectTask : DefaultTask() {

    @TaskAction
    fun action() {
        logger.lifecycle("Performance plugin is deprecated. If you see this message, consider removing performance plugin")
    }
}
