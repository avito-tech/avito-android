package com.avito.android.build_trace.internal

import com.avito.composite_exception.CompositeException
import org.gradle.api.Task
import org.gradle.api.internal.tasks.TaskDependencyResolveException

/**
 * Resolving dependencies shown itself as fragile
 * It can lead to unintended but inevitable errors.
 * They are not flaky but in rare unknown conditions with unknown workaround:
 * - https://issuetracker.google.com/issues/188457864
 */
internal sealed class TaskDependenciesResolutionResult(
    val tasks: Set<Task>,
    val suppressedErrors: List<Throwable>
) {

    class Success(
        tasks: Set<Task>,
        suppressedErrors: List<Throwable>
    ) : TaskDependenciesResolutionResult(tasks, suppressedErrors)

    class Failed(
        tasks: Set<Task>,
        suppressedErrors: List<Throwable>,
        val unexpectedError: CompositeException
    ) : TaskDependenciesResolutionResult(tasks, suppressedErrors)

    companion object {

        fun create(tasks: Set<Task>, resolutionErrors: List<Throwable>): TaskDependenciesResolutionResult {
            val suppressedErrors: List<Throwable> = resolutionErrors.mapNotNull { it.getKnownCauseOrNull() }
            val unexpectedErrors: List<Throwable> = resolutionErrors.filter { it.getKnownCauseOrNull() == null }

            return if (unexpectedErrors.isEmpty()) {
                Success(tasks, suppressedErrors)
            } else {
                Failed(tasks, suppressedErrors, unexpectedError(unexpectedErrors))
            }
        }

        private fun unexpectedError(reasons: List<Throwable>): CompositeException {
            val message = "Unexpected dependency resolution errors. " +
                "Possible solution: suppress it explicitly. See TaskDependenciesResolutionResult implementation."
            return CompositeException(message, throwables = reasons.toTypedArray())
        }

        private fun Throwable.getKnownCauseOrNull(): Throwable? {
            val cause = cause
            // https://issuetracker.google.com/issues/188457864
            if (this is TaskDependencyResolveException
                && cause is IllegalArgumentException
                && cause.message.orEmpty()
                    .contains("Cannot resolve object of unknown type CalculatedValueContainer to a Task")
            ) {
                return cause
            }
            return null
        }
    }
}
