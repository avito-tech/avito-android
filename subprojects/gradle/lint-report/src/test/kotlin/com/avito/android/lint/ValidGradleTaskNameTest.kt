package com.avito.android.lint

import com.avito.android.lint.internal.validInGradleTaskName
import com.avito.slack.model.SlackChannel
import com.avito.slack.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

internal class ValidGradleTaskNameTest {

    data class Case(val input: String, val expectedOutput: String)

    @TestFactory
    fun `validInGradleTaskName - for various slack channels`(): List<DynamicTest> = listOf(
        Case("#android-dev", "AndroidDev"),
        Case("#regression-android", "RegressionAndroid"),
        Case(slackChannel.id, slackChannel.id)
    )
        .map { case ->
            dynamicTest("${case.input} become ${case.expectedOutput}") {
                assertThat(case.input.validInGradleTaskName()).isEqualTo(case.expectedOutput)
            }
        }
}

private val slackChannel = SlackChannel.createStubInstance()
