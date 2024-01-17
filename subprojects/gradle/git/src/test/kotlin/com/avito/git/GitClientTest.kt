package com.avito.git

import com.avito.test.gradle.commit
import com.avito.test.gradle.file
import com.avito.test.gradle.git
import com.google.common.truth.Truth
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class GitClientTest {
    private lateinit var remoteRepo: File
    private lateinit var localRepo: File

    private val localClient by lazy { GitClient(localRepo) }
    private val remoteClient by lazy { GitClient(remoteRepo) }

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        val tmpDir = tempDir.toFile()

        remoteRepo = File(tmpDir, "remote").apply {
            mkdirs()
            initRemoteRepository(this)
        }
        localRepo = File(tmpDir, "local").apply {
            mkdirs()
        }
        tmpDir.git("clone remote local")
        with(localRepo) {
            git("checkout develop")
            git("branch -u origin/develop")
        }
    }

    private fun initRemoteRepository(dir: File) {
        with(dir) {
            git("init --quiet")
            file("data")
            commit("initial")
            git("branch develop")
        }
    }

    @Test
    fun `head commit hashes match`() {
        Truth.assertThat(localClient.headHashFull().getOrThrow())
            .isEqualTo(localRepo.git("rev-parse HEAD"))
        Truth.assertThat(localClient.headHashShort().getOrThrow())
            .isEqualTo(localRepo.git("rev-parse --short HEAD"))
    }

    @Test
    fun `current branch name matches`() {
        Truth.assertThat(localClient.currentBranchName().getOrThrow())
            .isEqualTo("develop")
    }

    @Test
    fun `tags set on remote branch are correctly red on local`() {
        val tags = listOf("tag-set-on-remote", "tag-set-on-remote-2")

        for (tag in tags) {
            remoteClient.commitTag(tag)
        }
        localRepo.git("fetch --tags")

        Truth.assertThat(localClient.tagsAtCurrentBranch().getOrThrow())
            .isEqualTo(tags)
    }

    @Test
    fun `local repo commits and pushes changes to remote repo`() {
        val commitMessage = "commit message"
        val createdFile = localRepo.file("shiny-file")
        localClient.commitSingleFile(createdFile.toPath(), commitMessage).getOrThrow()
        localClient.push("develop").getOrThrow()

        remoteRepo.git("checkout develop")
        remoteRepo.git("fetch")
        val remoteHeadCommitMessage = remoteRepo.git("log -1 --pretty=%B")
        Truth.assertThat(remoteHeadCommitMessage)
            .isEqualTo(commitMessage)
    }

    @Test
    fun `tags added to local repo are pushed to remote repo`() {
        val tags = listOf("tag-set-on-remote", "tag-set-on-remote-2")
        for (tag in tags) {
            localClient.commitTag(tag)
            localClient.pushRemoteTag(tag)
        }

        Truth.assertThat(localClient.tagsAtCurrentBranch().getOrThrow())
            .isEqualTo(tags)

        remoteRepo.git("checkout develop")
        remoteRepo.git("fetch")
        Truth.assertThat(remoteClient.tagsAtCurrentBranch().getOrThrow())
            .isEqualTo(tags)
    }
}
