package com.avito.android.string_transform.internal.task.apk

import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class WorkspacePathRenamerTest {

    private val renamer = WorkspacePathRenamer()

    @Test
    fun `rename - renames nested directories and files bottom-up - when path segments contain matching literals`(
        @TempDir dir: File,
    ) {
        dir.resolve("before")
            .apply { mkdirs() }
            .resolve("payload-before.txt")
            .writeText("content")

        val warnings = renamer.rename(
            workspaceDirectory = dir,
            rules = listOf(NormalizedRule(from = "before", to = "after")),
        ).getOrThrow()

        assertThat(dir.resolve("after").isDirectory).isTrue()
        assertThat(dir.resolve("after/payload-after.txt").isFile).isTrue()
        assertThat(dir.resolve("before").exists()).isFalse()
        assertThat(warnings).isEmpty()
    }

    @Test
    fun `rename - skips rule and returns warning - when target contains unsupported filename characters`(
        @TempDir dir: File,
    ) {
        dir.resolve("before").mkdirs()

        val warnings = renamer.rename(
            workspaceDirectory = dir,
            rules = listOf(NormalizedRule(from = "before", to = "after/name")),
        ).getOrThrow()

        assertThat(dir.resolve("before").exists()).isTrue()
        assertThat(dir.resolve("after").exists()).isFalse()
        assertThat(warnings).containsExactly(
            OperationWarning(
                message = "Rename rule 'before' -> 'after/name' is ignored for paths " +
                    "because target contains unsupported filename characters.",
            )
        )
    }
}
