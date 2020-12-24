package com.avito.git

import com.avito.logger.StubLoggerFactory
import com.avito.test.gradle.commit
import com.avito.test.gradle.file
import com.avito.test.gradle.git
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class GitStateFromEnvironmentTest {

    private val loggerFactory = StubLoggerFactory
    private lateinit var remoteRepo: File
    private lateinit var localRepo: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        val tmpDir = tempDir.toFile()

        remoteRepo = File(tmpDir, "remote").apply {
            mkdirs()
            initSampleRepository(this)
        }
        localRepo = File(tmpDir, "local").apply {
            mkdirs()
        }
        tmpDir.git("clone remote local")
        with(localRepo) {
            git("checkout -b develop")
            git("branch -u origin/develop")

            git("checkout -b feature")
            git("branch -u origin/feature")
            git("checkout master")
        }
    }

    private fun initSampleRepository(dir: File) {
        with(dir) {
            git("init --quiet")
            file("data")
            commit("initial")
            git("branch develop")
            git("branch feature")
        }
    }

    @Test
    fun `on target branch`() {
        localRepo.git("checkout develop")

        val state = gitState(gitBranch = "develop", targetBranch = null)

        assertThat(state.currentBranch.name).isEqualTo("develop")
        assertThat(state.targetBranch).isNull()
        assertThat(state.isOnDefaultBranch).isTrue()
    }

    @Test
    fun `on feature branch - no target branch - without target branch reference`() {
        localRepo.git("checkout feature")

        val state = gitState(gitBranch = "feature", targetBranch = null)

        assertThat(state.currentBranch.name).isEqualTo("feature")
        assertThat(state.targetBranch).isNull()
        assertThat(state.isOnDefaultBranch).isFalse()
    }

    @Test
    fun `on feature branch - use origin target reference - if available`() {
        val remoteFeatureCommit: String
        with(remoteRepo) {
            git("checkout develop")
            file("remote_changes")
            commit("remote changes in target branch")
            remoteFeatureCommit = git("rev-parse HEAD")
        }
        with(localRepo) {
            git("fetch --all")
            git("checkout feature")
        }
        val state = gitState(gitBranch = "feature", targetBranch = "develop")

        assertThat(state.currentBranch.name).isEqualTo("feature")
        assertThat(state.targetBranch).isNotNull()
        assertThat(state.targetBranch?.name).isEqualTo("develop")
        assertThat(state.targetBranch?.commit).isEqualTo(remoteFeatureCommit)
        assertThat(state.isOnDefaultBranch).isFalse()
    }

    @Test
    fun `on feature branch - use local target reference - if origin is not available`() {
        with(remoteRepo) {
            git("checkout develop")
            file("remote_changes")
            commit("remote changes in target branch")
        }
        val localFeatureCommit: String
        with(localRepo) {
            git("remote rm origin")
            git("fetch --all")
            git("checkout feature")
            localFeatureCommit = git("rev-parse HEAD")
        }
        val state = gitState(gitBranch = "feature", targetBranch = "develop")

        assertThat(state.currentBranch.name).isEqualTo("feature")
        assertThat(state.targetBranch).isNotNull()
        assertThat(state.targetBranch?.name).isEqualTo("develop")
        assertThat(state.targetBranch?.commit).isEqualTo(localFeatureCommit)
        assertThat(state.isOnDefaultBranch).isFalse()
    }

    private fun gitState(gitBranch: String, targetBranch: String?): GitState = GitStateFromEnvironment(
        rootDir = localRepo,
        gitBranch = gitBranch,
        targetBranch = targetBranch,
        originalCommitHash = null,
        loggerFactory = loggerFactory
    )
}
