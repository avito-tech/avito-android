package com.avito.test.gradle

import com.avito.test.gradle.TestResult.ExpectedFailure
import com.avito.truth.isInstanceOf
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Ordered
import com.google.common.truth.Subject
import com.google.common.truth.Subject.Factory
import com.google.common.truth.Truth
import org.gradle.testkit.runner.TaskOutcome

class TestResultSubject private constructor(
    failureMetadata: FailureMetadata,
    private val subject: TestResult
) : Subject(failureMetadata, subject) {

    fun buildSuccessful(): TestResultSubject {
        check("buildSuccessful").that(subject).isInstanceOf<TestResult.Success>()
        return this
    }

    fun outputContains(substring: String): TestResultSubject {
        check("output contains").that(subject.output).contains(substring)
        return this
    }

    fun configurationCachedReused(): TestResultSubject {
        check("configuration cache reused").that(subject.output).contains("Reusing configuration cache.")
        return this
    }

    fun outputDoesNotContain(substring: String): TestResultSubject {
        check("output does not contain").that(subject.output).doesNotContain(substring)
        return this
    }

    fun taskWithOutcome(taskPath: String, outcome: TaskOutcome): TestResultSubject {
        val actualOutcome: TaskOutcome? = subject.task(taskPath)?.outcome
        if (actualOutcome == null) {
            check("task $taskPath is executed")
                .that(actualOutcome as Any?).isNotNull()
        } else {
            check("task $taskPath has outcome ${outcome.name}")
                .that(actualOutcome).isEquivalentAccordingToCompareTo(outcome)
        }
        return this
    }

    fun buildFailed(): TestResultSubject {
        check("buildFailed").that(subject).isInstanceOf<ExpectedFailure>()
        return this
    }

    fun tasksShouldBeTriggered(vararg taskPath: String): Ordered =
        check("tasks should be triggered").that(subject.allTaskPaths).containsAtLeastElementsIn(taskPath)

    fun tasksShouldNotBeTriggered(vararg taskPath: String) {
        check("tasks should not be triggered").that(subject.allTaskPaths).containsNoneIn(taskPath)
    }

    fun moduleTaskShouldNotBeTriggered(vararg modulePaths: String) {
        check("modules tasks should not be triggered").that(subject.triggeredModules).containsNoneIn(modulePaths)
    }

    companion object {
        private val SUBJECT_FACTORY =
            Factory<TestResultSubject, TestResult> { metadata, actual ->
                TestResultSubject(metadata, actual)
            }

        fun assertThat(subject: TestResult): TestResultSubject =
            Truth.assertAbout(SUBJECT_FACTORY).that(subject)
    }
}
