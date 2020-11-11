package com.avito.android.lint

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

internal class ValidGradleTaskNameTest {

    data class Case(val input: String, val expectedOutput: String)

    @TestFactory
    fun `validInGradleTaskName - for various slack channels`(): List<DynamicTest> = listOf(
        Case("#android-dev", "AndroidDev"),
        Case("#regression-android", "RegressionAndroid")
    )
        .map { case ->
            dynamicTest("${case.input} become ${case.expectedOutput}") {
                assertThat(case.input.validInGradleTaskName()).isEqualTo(case.expectedOutput)
            }
        }
}
