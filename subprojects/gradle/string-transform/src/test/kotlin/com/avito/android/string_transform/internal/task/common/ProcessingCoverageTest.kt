package com.avito.android.string_transform.internal.task.common

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ProcessingCoverageTest {

    @Test
    fun `processing coverage - excludes handled and skipped files - when residual candidates are checked`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply {
            resolve("base/assets").mkdirs()
            resolve("base/assets/handled.txt").writeText("handled")
            resolve("base/assets/skipped.txt").writeText("skipped")
            resolve("base/assets/residual.txt").writeText("residual")
        }
        val handledFile = workspace.resolve("base/assets/handled.txt")
        val skippedFile = workspace.resolve("base/assets/skipped.txt")
        val residualFile = workspace.resolve("base/assets/residual.txt")
        val coverage = ProcessingCoverage(workspace)

        coverage.markHandled(handledFile)
        coverage.markSkipped(skippedFile)

        assertThat(coverage.isResidualCandidate(handledFile)).isFalse()
        assertThat(coverage.isResidualCandidate(skippedFile)).isFalse()
        assertThat(coverage.isResidualCandidate(residualFile)).isTrue()
    }

    @Test
    fun `processing coverage - remains idempotent - when same file is marked multiple times`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply {
            resolve("base/assets").mkdirs()
            resolve("base/assets/handled.txt").writeText("handled")
        }
        val handledFile = workspace.resolve("base/assets/handled.txt")
        val coverage = ProcessingCoverage(workspace)

        coverage.markHandled(handledFile)
        coverage.markHandled(handledFile)

        assertThat(coverage.isResidualCandidate(handledFile)).isFalse()
    }

    @Test
    fun `processing coverage - resolves nested workspace paths - when residual candidates are checked`(
        @TempDir dir: File,
    ) {
        val workspace = dir.resolve("workspace").apply {
            resolve("feature/assets/nested").mkdirs()
            resolve("feature/assets/nested/file.txt").writeText("payload")
        }
        val nestedFile = workspace.resolve("feature/assets/nested/file.txt")
        val coverage = ProcessingCoverage(workspace)

        coverage.markSkipped(nestedFile)

        assertThat(coverage.isResidualCandidate(nestedFile)).isFalse()
    }
}
