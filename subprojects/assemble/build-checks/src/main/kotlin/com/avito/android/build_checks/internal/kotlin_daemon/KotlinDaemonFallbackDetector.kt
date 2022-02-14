package com.avito.android.build_checks.internal.kotlin_daemon

import org.gradle.api.Project
import org.gradle.internal.logging.LoggingManagerInternal
import org.gradle.kotlin.dsl.support.serviceOf
import java.util.concurrent.atomic.AtomicInteger

internal class KotlinDaemonFallbackDetector {

    fun register(project: Project) {
        if (isDaemonDisabled(project)) {
            project.logger.lifecycle("Kotlin daemon fallback detection is disabled due to absence of daemon")
            return
        }
        val fallbacksCounter = AtomicInteger(0)

        val loggingManager = project.gradle.serviceOf<LoggingManagerInternal>()
        val listenerManager = project.gradle.buildOperationListenerManager()

        val failureListener = FailureEventListener(fallbacksCounter)
        loggingManager.addOutputEventListener(failureListener)

        val buildFailer = BuildFailer(fallbacksCounter)
        buildFailer.cleanupAction = {
            loggingManager.removeOutputEventListener(failureListener)
            listenerManager.removeListener(buildFailer)
        }
        listenerManager.addListener(buildFailer)
    }

    /**
     * Copy of internal logic in GradleKotlinCompilerRunner
     */
    private fun isDaemonDisabled(project: Project): Boolean {
        val strategy = project.providers.systemProperty("kotlin.compiler.execution.strategy")
            .forUseAtConfigurationTime()
            .getOrElse("daemon")
        return strategy != "daemon" // "in-process", "out-of-process"
    }
}
