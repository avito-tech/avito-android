package com.avito.android.baseline_profile

import com.avito.android.baseline_profile.configuration.SaveProfileToVersionControlSettings
import com.avito.android.baseline_profile.internal.BaselineProfileGitClient
import com.avito.git.GitClient
import com.avito.logger.PrintlnLoggerFactory
import com.avito.test.gradle.commit
import com.avito.test.gradle.file
import com.avito.test.gradle.git
import com.google.common.truth.Truth
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.util.UUID

class BaselineProfileGitClientTest {
    private val expectedTag = "baseline-profile-develop"

    private lateinit var remoteRepo: File
    private lateinit var localRepo: File
    private val localClient by lazy { GitClient(localRepo) }

    private lateinit var profileGitClient: BaselineProfileGitClient

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

        profileGitClient = BaselineProfileGitClient(
            localRepo,
            object : SaveProfileToVersionControlSettings {
                override val enable = true
                override val enableRemoteOperations: Boolean = true
                override val commitMessage: String = "Baseline Profile updated"
                override val includeDetailsInCommitMessage: Boolean = true
            },
            PrintlnLoggerFactory
        )
    }

    private fun initRemoteRepository(dir: File) {
        with(dir) {
            git("init --quiet --initial-branch=master")
            file("data")
            commit("initial")
            git("branch develop")
        }
    }

    @Test
    fun `local head commit tags are empty - profile tag not found`() {
        Truth.assertThat(profileGitClient.isHeadCommitWithProfile())
            .isFalse()
    }

    @Test
    fun `local head commit tags are set - profile tag not found`() {
        GitClient(localRepo).commitTag("non-relevant-tag")

        Truth.assertThat(profileGitClient.isHeadCommitWithProfile())
            .isFalse()
    }

    @Test
    fun `local head commit tag is set correctly - profile tag is found`() {
        GitClient(localRepo).commitTag("baseline-profile-develop")

        Truth.assertThat(profileGitClient.isHeadCommitWithProfile())
            .isTrue()
    }

    @Test
    fun `applying profile - commit message is valid, tags are set`() {
        val headHash = localClient.headHashShort().getOrThrow()

        commitAndPushProfile()

        Truth.assertThat(localRepo.commitsOneLiners())
            .containsExactly(
                "(HEAD -> refs/heads/develop, tag: refs/tags/$expectedTag, refs/remotes/origin/develop) " +
                    "Baseline Profile updated (develop/$headHash)",
                "(refs/remotes/origin/master, refs/remotes/origin/HEAD, refs/heads/master) initial"
            ).inOrder()
    }

    @Test
    fun `applying profile - push to remote is made, tags are updated`() {
        val headHash = localClient.headHashShort().getOrThrow()

        commitAndPushProfile()

        remoteRepo.git("checkout develop")
        remoteRepo.git("fetch")
        Truth.assertThat(remoteRepo.commitsOneLiners())
            .containsExactly(
                "(HEAD -> refs/heads/develop, tag: refs/tags/$expectedTag) " +
                    "Baseline Profile updated (develop/$headHash)",
                "(refs/heads/master) initial"
            ).inOrder()
    }

    @Test
    fun `applying profile after changes - push to remote is made, old tag is removed`() {
        val initialCommitHash = localClient.headHashShort().getOrThrow()

        commitAndPushProfile()
        commitAndPushUnrelatedFile()
        val codeChangesCommitHash = localClient.headHashShort().getOrThrow()
        commitAndPushProfile()

        remoteRepo.git("checkout develop")
        remoteRepo.git("fetch")
        Truth.assertThat(remoteRepo.commitsOneLiners())
            .containsExactly(
                "(HEAD -> refs/heads/develop, tag: refs/tags/$expectedTag) " +
                    "Baseline Profile updated (develop/$codeChangesCommitHash)",
                "actual_code_changes",
                "Baseline Profile updated (develop/$initialCommitHash)",
                "(refs/heads/master) initial"
            ).inOrder()
    }

    private fun commitAndPushProfile() {
        val file = localRepo.file("baseline-profile.txt", content = UUID.randomUUID().toString())
        profileGitClient.commitAndPushProfile(file.toPath())
    }

    private fun commitAndPushUnrelatedFile() {
        localRepo.file("SomeCodeChanges.kt")
        localRepo.commit("actual code changes")
    }

    private fun File.commitsOneLiners(): List<String> =
        git("log --tags --pretty=\"%d %s\" --decorate=full")
            .split("\n")
            .map { it.trim() }
}
