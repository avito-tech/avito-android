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

class GitLocalStateTest {

    private val loggerFactory = StubLoggerFactory
    private lateinit var repoDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        repoDir = tempDir.toFile()

        with(repoDir) {
            git("init --quiet")
            file("data")
            commit("initial")
            git("branch develop")
            git("branch feature")
        }
    }

    @Test
    fun `on target branch`() {
        repoDir.git("checkout develop")

        val state = gitState(targetBranch = null)

        assertThat(state.currentBranch.name).isEqualTo("develop")
        assertThat(state.targetBranch).isNull()
        assertThat(state.isOnDefaultBranch).isTrue()
    }

    @Test
    fun `on feature branch - without target branch reference`() {
        repoDir.git("checkout feature")

        val state = gitState(targetBranch = null)

        assertThat(state.currentBranch.name).isEqualTo("feature")
        assertThat(state.targetBranch).isNull()
        assertThat(state.isOnDefaultBranch).isFalse()
    }

    @Test
    fun `on feature branch - with target branch reference`() {
        repoDir.git("checkout feature")

        val state = gitState(targetBranch = "develop")

        assertThat(state.currentBranch.name).isEqualTo("feature")
        assertThat(state.targetBranch?.name).isEqualTo("develop")
        assertThat(state.isOnDefaultBranch).isFalse()
    }

    private fun gitState(targetBranch: String?): GitState = GitLocalStateImpl(
        rootDir = repoDir,
        targetBranch = targetBranch,
        loggerFactory = loggerFactory
    )
}
