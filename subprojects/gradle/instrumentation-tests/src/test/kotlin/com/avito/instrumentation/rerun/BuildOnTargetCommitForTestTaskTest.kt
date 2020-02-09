package com.avito.instrumentation.rerun

import com.avito.git.Git
import com.avito.instrumentation.minimalInstrumentationPluginConfiguration
import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.file
import com.avito.utils.logging.CILogger
import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths

internal class BuildOnTargetCommitForTestTaskTest {

    private lateinit var tempDir: File
    private val git: Git by lazy { Git.Impl(tempDir) { CILogger.allToStdout.info(it)} }

    private val syncBranch = "develop"

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        this.tempDir = tempDir
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = listOf("com.avito.android.instrumentation-tests"),
                    buildGradleExtra = minimalInstrumentationPluginConfiguration
                )
            ),
            localBuildCache = File(tempDir, "local-build-cache").apply { mkdirs() }
        ).generateIn(tempDir)

        with(git) {
            init()
            checkout(branchName = syncBranch, create = true)
            addAll()
            commit("initial commit")
        }
    }

    @Test
    fun `instrumentation task - is loaded from cache`() {
        val versionName = "123"
        val versionCode = "1"

        tempDir.file("newfile.md", "1234")

        git.checkout("new-branch", true)
        git.addAll()
        git.commit("new file added")

        val result = runBuildOnTargetCommit(
            currentBranch = "new-branch",
            versionName = versionName,
            versionCode = versionCode
        )

        result.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:buildOnTargetCommit", TaskOutcome.SUCCESS)

        val nestedBuildPath = Paths.get(tempDir.path, "app", "nested-build")

        nestedBuildPath.toFile().deleteRecursively()

        val secondRunResult = runBuildOnTargetCommit(
            currentBranch = "new-branch",
            versionName = versionName,
            versionCode = versionCode
        )

        secondRunResult.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:buildOnTargetCommit", TaskOutcome.FROM_CACHE)

        val mainApkPath = nestedBuildPath.resolve(Paths.get("app", "build", "outputs", "apk", "debug"))
        val testApkPath = nestedBuildPath.resolve(Paths.get("app", "build", "outputs", "apk", "androidTest", "debug"))
        assertThat(File(mainApkPath.toFile(), "app-debug.apk").readText()).isEqualTo("stub")
        assertThat(File(testApkPath.toFile(), "app-debug-androidTest.apk").readText()).isEqualTo("stub")
    }

    @Test
    fun `instrumentation task - misses cache for new commit hash`(@TempDir tempDir: File) {
        val versionName = "123"
        val versionCode = "1"

        tempDir.file("newfile.md", "1234")

        git.checkout("new-branch", true)
        git.addAll()
        git.commit("new file added")

        val result = runBuildOnTargetCommit(
            currentBranch = "new-branch",
            versionName = versionName,
            versionCode = versionCode
        )

        result.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:buildOnTargetCommit", TaskOutcome.SUCCESS)

        val nestedBuildPath = Paths.get(tempDir.path, "app", "nested-build")

        nestedBuildPath.toFile().deleteRecursively()

        git.checkout(syncBranch, create = false)

        tempDir.file("newChangeInTarget.md", "1234")
        git.addAll()
        git.commit("newChangeInTarget")

        git.checkout("new-branch", create = false)

        val secondRunResult = runBuildOnTargetCommit(
            currentBranch = "new-branch",
            versionName = versionName,
            versionCode = versionCode
        )

        secondRunResult.assertThat()
            .buildSuccessful()
            .taskWithOutcome(":app:buildOnTargetCommit", TaskOutcome.SUCCESS)
    }

    @Suppress("SameParameterValue")
    private fun runBuildOnTargetCommit(currentBranch: String, versionName: String, versionCode: String): TestResult {
        return ciRun(
            tempDir,
            "--build-cache",
            "app:buildOnTargetCommit",
            "-PdeviceName=LOCAL",
            "-PteamcityBuildId=0",
            "-Papp.versionName=$versionName",
            "-Papp.versionCode=$versionCode",
            "-PstubBuildOnTargetCommit=true",
            "-Pavito.repo.ssh.url=git.ssh",
            branch = currentBranch,
            targetBranch = syncBranch
        )
    }
}
