package com.avito.android.critical_path

import com.avito.composite_exception.CompositeException
import org.gradle.api.Task
import org.gradle.api.internal.tasks.TaskDependencyResolveException

/**
 * Resolving dependencies shown itself as fragile
 * It can lead to unintended but inevitable errors.
 * They are not flaky but in rare unknown conditions with unknown workaround:
 * - https://issuetracker.google.com/issues/188457864
 */
public sealed class TaskDependenciesResolutionResult {

    public class Success(public val tasks: Set<Task>) : TaskDependenciesResolutionResult()

    public class Failed(public val problem: CompositeException) : TaskDependenciesResolutionResult()

    public companion object {

        public fun create(
            dependants: Set<Task>,
            resolutionErrors: List<Throwable>
        ): TaskDependenciesResolutionResult {
            val errors: List<Throwable> = resolutionErrors.filter { it.getKnownCauseOrNull() == null }

            return if (errors.isEmpty()) {
                Success(dependants)
            } else {
                Failed(unexpectedError(errors))
            }
        }

        private fun unexpectedError(reasons: List<Throwable>) = CompositeException(
            "Unexpected dependency resolution errors",
            throwables = reasons.toTypedArray()
        )

        private fun Throwable.getKnownCauseOrNull(): Throwable? {
            val cause = cause
            // https://github.com/gradle/gradle/issues/17287
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
