package com.avito.android.build_checks.internal.kotlin_daemon

import com.avito.android.build_checks.RootProjectChecksExtension
import com.avito.android.build_checks.internal.FailedCheckMessage
import org.gradle.api.GradleException
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationType
import org.gradle.api.invocation.Gradle
import org.gradle.execution.RunRootBuildWorkBuildOperationType
import org.gradle.internal.operations.BuildOperationCategory
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.BuildOperationListener
import org.gradle.internal.operations.BuildOperationListenerManager
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.internal.operations.OperationProgressEvent
import org.gradle.internal.operations.OperationStartEvent
import java.util.concurrent.atomic.AtomicInteger

internal class BuildFailer(
    private val fallbackCounter: AtomicInteger,
) : BuildOperationListener {

    var cleanupAction: (() -> Unit)? = null

    override fun started(buildOperation: BuildOperationDescriptor, event: OperationStartEvent) {
    }

    override fun progress(operationIdentifier: OperationIdentifier, progressEvent: OperationProgressEvent) {
    }

    override fun finished(buildOperation: BuildOperationDescriptor, event: OperationFinishEvent) {
        if (isTaskFinished(event) && fallbackCounter.get() > FALLBACKS_COUNT_THRESHOLD) {
            doCleanup()
            failBuild()
        }
        if (isBuildFinished(buildOperation)) {
            doCleanup()
        }
    }

    @Suppress("MaxLineLength")
    private fun failBuild() {
        throw GradleException(
            FailedCheckMessage(
                RootProjectChecksExtension::preventKotlinDaemonFallback,
                """
                |Kotlin daemon process is not available and most probably won't recover on its own.
                |It has incredible impact on build performance and continuing build is not worth it.
                |https://youtrack.jetbrains.com/issue/KT-48843
                |
                |How to fix
                |
                |Kill Kotlin daemon process:
                |   1. ./gradlew --stop
                |   2. Find Kotlin daemon process id (pid): `jps | grep Kotlin`
                |   3. kill <pid>
                |   
                |If it doesn't help, check that there are no custom jvm arguments in "kotlin.daemon.jvm.options" property except for Xmx.
                """.trimMargin()
            ).toString()
        )
    }

    private fun doCleanup() {
        val action = requireNotNull(cleanupAction) {
            "cleanupAction must be set to unsubscribe Gradle listeners"
        }
        action()
    }

    private fun isTaskFinished(event: OperationFinishEvent): Boolean =
        event.result is ExecuteTaskBuildOperationType.Result

    private fun isBuildFinished(operation: BuildOperationDescriptor): Boolean =
        operation.details is RunRootBuildWorkBuildOperationType.Details
            && operation.metadata == BuildOperationCategory.RUN_WORK
}

/**
 * To be more confident that daemon process is non recoverable
 */
private const val FALLBACKS_COUNT_THRESHOLD = 1

internal fun Gradle.buildOperationListenerManager(): BuildOperationListenerManager =
    (this as GradleInternal).services[BuildOperationListenerManager::class.java]
