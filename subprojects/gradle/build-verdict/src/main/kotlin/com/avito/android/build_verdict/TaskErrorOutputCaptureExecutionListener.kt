package com.avito.android.build_verdict

import com.avito.utils.logging.CILogger
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.util.Path
import org.jetbrains.kotlin.gradle.internal.KaptTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal class TaskErrorOutputCaptureExecutionListener(
    private val logs: MutableMap<Path, StringBuilder>,
    private val logger: CILogger
) : TaskExecutionListener, BuildAdapter() {

    override fun beforeExecute(task: Task) {
        when (task) {
            is Test -> {
                logger.debug("Add error listener to the task ${task.path}")
                task.addTestListener(object : DefaultTestListener() {
                    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
                        if (result.resultType == TestResult.ResultType.FAILURE) {
                            logs.getOrPut(Path.path(task.path), { StringBuilder("FAILED tests:\n") })
                                .appendln("\t${testDescriptor.className}.${testDescriptor.displayName}")
                        }
                    }
                })
            }
            is KaptTask,
            is KotlinCompile -> {
                logger.debug("Add error listener to the task ${task.path}")
                task.logging.addStandardErrorListener { error ->
                    logs.getOrPut(Path.path(task.path), { StringBuilder() })
                        .append(error)
                }
            }
        }
    }

    override fun afterExecute(task: Task, state: TaskState) {
        if (state.failure == null) {
            logs.remove(Path.path(task.path))
        } else {
            when (task) {
                is BuildVerdictTask -> {
                    logger.debug("Get a verdict from the task ${task.path}")
                    logs.getOrPut(Path.path(task.path), { StringBuilder() })
                        .append(task.verdict)
                }

            }
        }
    }

    override fun buildFinished(result: BuildResult) {
        result.gradle?.removeListener(this)
    }
}
