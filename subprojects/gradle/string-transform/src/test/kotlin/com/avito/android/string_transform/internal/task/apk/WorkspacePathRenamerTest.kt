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

        val result = renamer.rename(
            workspaceDirectory = dir,
            rules = listOf(NormalizedRule(from = "before", to = "after")),
        ).getOrThrow()

        assertThat(dir.resolve("after").isDirectory).isTrue()
        assertThat(dir.resolve("after/payload-after.txt").isFile).isTrue()
        assertThat(dir.resolve("before").exists()).isFalse()
        assertThat(result.warnings).isEmpty()
    }

    @Test
    fun `rename - skips rule and returns warning - when target contains unsupported filename characters`(
        @TempDir dir: File,
    ) {
        dir.resolve("before").mkdirs()

        val result = renamer.rename(
            workspaceDirectory = dir,
            rules = listOf(NormalizedRule(from = "before", to = "after/name")),
        ).getOrThrow()

        assertThat(dir.resolve("before").exists()).isTrue()
        assertThat(dir.resolve("after").exists()).isFalse()
        assertThat(result.warnings).containsExactly(
            OperationWarning(
                message = "Rename rule 'before' -> 'after/name' is ignored for paths " +
                    "because target contains unsupported filename characters.",
            )
        )
    }

    @Test
    fun `rename - returns pathMapping for renamed file`(
        @TempDir dir: File,
    ) {
        dir.resolve("hello.txt").writeText("data")

        val result = renamer.rename(
            workspaceDirectory = dir,
            rules = listOf(NormalizedRule(from = "hello", to = "world")),
        ).getOrThrow()

        assertThat(result.pathMapping).containsExactly("hello.txt", "world.txt")
    }

    @Test
    fun `rename - maps both directory and file segments - when STORED entry is under renamed directory`(
        @TempDir dir: File,
    ) {
        dir.resolve("lib/old-abi")
            .apply { mkdirs() }
            .resolve("old-name.so")
            .writeText("native")

        val result = renamer.rename(
            workspaceDirectory = dir,
            rules = listOf(NormalizedRule(from = "old", to = "new")),
        ).getOrThrow()

        assertThat(dir.resolve("lib/new-abi/new-name.so").isFile).isTrue()
        assertThat(dir.resolve("lib/old-abi").exists()).isFalse()
        assertThat(result.pathMapping).containsExactly(
            "lib/old-abi/old-name.so", "lib/new-abi/new-name.so"
        )
    }

    @Test
    fun `rename - returns empty pathMapping - when no paths change`(
        @TempDir dir: File,
    ) {
        dir.resolve("stable.txt").writeText("content")

        val result = renamer.rename(
            workspaceDirectory = dir,
            rules = listOf(NormalizedRule(from = "missing", to = "replacement")),
        ).getOrThrow()

        assertThat(result.pathMapping).isEmpty()
        assertThat(result.warnings).isEmpty()
    }

    @Test
    fun `rename - preserves warning about unsupported chars alongside valid pathMapping`(
        @TempDir dir: File,
    ) {
        dir.resolve("alpha.txt").writeText("data")

        val result = renamer.rename(
            workspaceDirectory = dir,
            rules = listOf(
                NormalizedRule(from = "bad", to = "bad/slash"),
                NormalizedRule(from = "alpha", to = "beta"),
            ),
        ).getOrThrow()

        assertThat(result.warnings).hasSize(1)
        assertThat(result.warnings[0].message).contains("unsupported filename characters")
        assertThat(result.pathMapping).containsExactly("alpha.txt", "beta.txt")
    }
}
