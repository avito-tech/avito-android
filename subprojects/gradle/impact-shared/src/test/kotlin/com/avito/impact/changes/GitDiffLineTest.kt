package com.avito.impact.changes

import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.funktionale.tries.Try
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class GitDiffLineTest {

    private class Case(val line: String, val expected: GitDiffLine)

    @TestFactory
    fun `parseGitLine - parses correctly`(): List<DynamicTest> {
        return listOf(
            Case("A test.kt", GitDiffLine("test.kt", ChangeType.ADDED)),
            Case("D test.kt", GitDiffLine("test.kt", ChangeType.DELETED)),
            Case("M test.kt", GitDiffLine("test.kt", ChangeType.MODIFIED)),
            Case("R095 old.kt new.kt", GitDiffLine("new.kt", ChangeType.RENAMED)),
            Case("C095 old.kt new.kt", GitDiffLine("new.kt", ChangeType.COPIED))
        )
            .map { case ->
                dynamicTest("${case.line} parses to ${case.expected.changeType}") {
                    val actual = case.line.parseGitDiffLine()
                    assertThat(actual.get()).isEqualTo(case.expected)
                }
            }
    }

    @TestFactory
    fun `parseGitLine - returns failure - line has invalid format`(): List<DynamicTest> {
        return listOf(
            "warning: inexact rename detection was skipped due to too many files.",
            "Some meaningless line in git output"
        )
            .map { line ->
                dynamicTest("'$line' parses to Failure") {
                    val actual = line.parseGitDiffLine()
                    assertThat(actual).isInstanceOf<Try.Failure<Any>>()
                }
            }
    }
}
