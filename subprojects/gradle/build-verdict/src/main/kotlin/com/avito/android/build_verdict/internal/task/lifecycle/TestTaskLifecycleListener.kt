package com.avito.android.build_verdict.internal.task.lifecycle

import com.avito.android.build_verdict.internal.DefaultTestListener
import com.avito.android.build_verdict.span.SpannedString.Companion.toSpanned
import com.avito.android.build_verdict.span.SpannedStringBuilder
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.util.Path

internal class TestTaskLifecycleListener(
    private val verdicts: MutableMap<Path, SpannedStringBuilder>
) : TaskLifecycleListener<Test>() {

    override val acceptedTask: Class<in Test> = Test::class.java

    override fun beforeExecuteTyped(task: Test) {
        task.addTestListener(
            object : DefaultTestListener() {
                override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
                    if (result.resultType == TestResult.ResultType.FAILURE) {
                        verdicts.getOrPut(Path.path(task.path), { SpannedStringBuilder("FAILED tests:".toSpanned()) })
                            .addLine("\t${testDescriptor.className}.${testDescriptor.displayName}".toSpanned())
                    }
                }
            }
        )
    }
}
