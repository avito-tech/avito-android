package com.avito.impact.changes

import com.avito.test.gradle.commit
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.git
import com.avito.test.gradle.mutate
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class ChangesDetectorTest {

    private lateinit var rootDir: File
    private lateinit var targetCommit: String

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        rootDir = tempDir.toFile()

        with(rootDir) {
            file(".gitignore")
            dir("project") {
                file("Deleted.kt", content = "file for deletion")
                file("README.md", content = "README")
                file("TestRenameOld.kt", content = "file for renaming")
            }
            file("README.md")
        }
        with(rootDir) {
            git("init --quiet")
            commit("initial_state")
            git("checkout -b develop")

            targetCommit = git("rev-parse HEAD")
        }

        with(rootDir) {
            git("checkout -b simple-changes develop")
            file("README.md").mutate()
            dir("project") {
                file("Deleted.kt").delete()
                file("README.md").mutate()
                file("New.kt", content = "new file").createNewFile()
                file("TestRenameOld.kt").delete()
                file("TestRenameNew.kt", content = "file for renaming")
            }
            commit()
        }
    }

    @Test
    fun `detect all changes - finds them all`() {
        val changedFiles = detectChanges(rootDir)

        assertThat(changedFiles).containsExactly(
            ChangedFile(rootDir, File(rootDir, "README.md"), ChangeType.MODIFIED),
            ChangedFile(rootDir, File(rootDir, "project/README.md"), ChangeType.MODIFIED),
            ChangedFile(rootDir, File(rootDir, "project/Deleted.kt"), ChangeType.DELETED),
            ChangedFile(rootDir, File(rootDir, "project/New.kt"), ChangeType.ADDED),
            ChangedFile(rootDir, File(rootDir, "project/TestRenameNew.kt"), ChangeType.RENAMED)
        )
    }

    @Test
    fun `detect all changes - ignore unstaged files`() {
        rootDir.dir("project") {
            file("Unstaged.kt", content = "unstaged file")
        }
        val changedFiles = detectChanges(rootDir)

        val unstagedChange = changedFiles.firstOrNull { it.relativePath == "Unstaged.kt" }
        assertWithMessage("Ignore unstaged files for now. We don't know how deal with them yet.")
            .that(unstagedChange).isNull()
    }

    @Test
    fun `detect changes in subdirectory - finds changes only in that directory`() {
        val changedFiles = detectChanges(rootDir, targetDir = rootDir.dir("project"))

        assertThat(changedFiles).containsExactly(
            ChangedFile(rootDir, File(rootDir, "project/README.md"), ChangeType.MODIFIED),
            ChangedFile(rootDir, File(rootDir, "project/Deleted.kt"), ChangeType.DELETED),
            ChangedFile(rootDir, File(rootDir, "project/New.kt"), ChangeType.ADDED),
            ChangedFile(rootDir, File(rootDir, "project/TestRenameNew.kt"), ChangeType.RENAMED)
        )
    }

    @Test
    fun `detect changes with exclusion - finds all changes without excluded directories`() {
        val changedFiles = detectChanges(
            rootDir,
            excludedDirectories = listOf(rootDir.dir("project"))
        )

        assertThat(changedFiles).contains(
            ChangedFile(rootDir, File(rootDir, "README.md"), ChangeType.MODIFIED)
        )
    }

    @Test
    fun `detect changes with custom ignore - finds all changes without ignored`() {
        val patterns = setOf("*.md", "project/*")
        val changedFiles = detectChanges(rootDir, ignorePatterns = patterns)

        assertThat(changedFiles).isEmpty()
    }

    private fun detectChanges(
        rootDir: File,
        targetDir: File = rootDir,
        excludedDirectories: List<File> = emptyList(),
        ignorePatterns: Set<String> = emptySet()
    ): List<ChangedFile> {
        return GitChangesDetector(
            gitRootDir = rootDir,
            targetCommit = targetCommit,
            ignoreSettings = IgnoreSettings(ignorePatterns),
        )
            .computeChanges(targetDir, excludedDirectories)
            .fold(
                { it },
                {
                    System.err.println(it.message)
                    emptyList()
                }
            )
    }
}
