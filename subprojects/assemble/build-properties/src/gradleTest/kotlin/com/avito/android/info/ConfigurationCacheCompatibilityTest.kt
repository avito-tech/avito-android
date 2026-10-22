package com.avito.android.info

import com.avito.git.Git
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.Properties

internal class ConfigurationCacheCompatibilityTest {

    @Test
    fun `configuration with applied plugin - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.build-properties")
                    }
                )
            )
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    /**
     * Proves the MBSA-2353 fix for `app-build-info.properties`: with `buildInfo.gitCommit`
     * unset, GIT_COMMIT/GIT_BRANCH resolve from GitInfoBuildService at task-execution time.
     * A `git commit` between two builds must NOT invalidate the configuration cache.
     */
    @Test
    fun `git commit between builds - generateAppBuildProperties CC reused`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.build-properties")
                    }
                )
            )
        ).generateIn(projectDir)

        val notes = File(projectDir, "notes.txt").apply { writeText("v1") }
        val git = Git.create(projectDir)
        git.init().getOrThrow()
        git.checkout(branchName = "feature", create = true).getOrThrow()
        git.addAll().getOrThrow()
        git.commit("initial").getOrThrow()

        runAppBuildProperties(projectDir).assertThat().buildSuccessful()

        notes.writeText("v2")
        git.commit("between builds").getOrThrow()

        runAppBuildProperties(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    @Test
    fun `git commit between builds - omitted git values - CC reused, values empty`(@TempDir projectDir: File) {
        generateAppWithBuildInfo(
            projectDir,
            buildInfo = """
                gitCommit = com.avito.android.info.GitValue.Omitted
                gitBranch = com.avito.android.info.GitValue.Omitted
            """.trimIndent()
        )
        val git = initGitRepo(projectDir)

        runAppBuildProperties(projectDir).assertThat().buildSuccessful()

        commitChange(projectDir, git)

        runAppBuildProperties(projectDir).assertThat().buildSuccessful().configurationCachedReused()
        val properties = readAppBuildInfo(projectDir)
        assertThat(properties.getProperty("GIT_COMMIT")).isEmpty()
        assertThat(properties.getProperty("GIT_BRANCH")).isEmpty()
    }

    @Test
    fun `git commit between builds - explicit git values - CC reused`(@TempDir projectDir: File) {
        generateAppWithBuildInfo(
            projectDir,
            buildInfo = """
                gitCommit = com.avito.android.info.GitValue.Explicit("commit")
                gitBranch = com.avito.android.info.GitValue.Explicit("branch")
            """.trimIndent()
        )
        val git = initGitRepo(projectDir)

        runAppBuildProperties(projectDir).assertThat().buildSuccessful()

        commitChange(projectDir, git)

        runAppBuildProperties(projectDir).assertThat().buildSuccessful().configurationCachedReused()
        val properties = readAppBuildInfo(projectDir)
        assertThat(properties.getProperty("GIT_COMMIT")).isEqualTo("commit")
        assertThat(properties.getProperty("GIT_BRANCH")).isEqualTo("branch")
    }

    @Test
    fun `git commit between builds - git values from git - updated without CC invalidation`(@TempDir projectDir: File) {
        generateAppWithBuildInfo(projectDir, buildInfo = "")
        val git = initGitRepo(projectDir)

        runAppBuildProperties(projectDir).assertThat().buildSuccessful()
        val firstCommit = readAppBuildInfo(projectDir).getProperty("GIT_COMMIT")

        commitChange(projectDir, git)

        runAppBuildProperties(projectDir).assertThat().buildSuccessful().configurationCachedReused()
        val properties = readAppBuildInfo(projectDir)
        assertThat(properties.getProperty("GIT_COMMIT")).isNotEqualTo(firstCommit)
        assertThat(properties.getProperty("GIT_BRANCH")).isEqualTo("feature")
    }

    private fun generateAppWithBuildInfo(projectDir: File, buildInfo: String) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    enableKotlinAndroidPlugin = false,
                    useKts = true,
                    plugins = plugins {
                        id("com.avito.android.build-properties")
                    },
                    buildGradleExtra = "buildInfo {\n$buildInfo\n}"
                )
            )
        ).generateIn(projectDir)
    }

    private fun initGitRepo(projectDir: File): Git {
        File(projectDir, "notes.txt").writeText("v1")
        val git = Git.create(projectDir)
        git.init().getOrThrow()
        git.checkout(branchName = "feature", create = true).getOrThrow()
        git.addAll().getOrThrow()
        git.commit("initial").getOrThrow()
        return git
    }

    private fun commitChange(projectDir: File, git: Git) {
        File(projectDir, "notes.txt").writeText("v2")
        git.addAll().getOrThrow()
        git.commit("between builds").getOrThrow()
    }

    private fun readAppBuildInfo(projectDir: File): Properties {
        return Properties().also { properties ->
            File(projectDir, "app/src/main/assets/app-build-info.properties").inputStream().use(properties::load)
        }
    }

    private fun runTask(projectDir: File): TestResult {
        return gradlew(
            projectDir,
            ":app:generateBuildProperties",
            dryRun = true,
            configurationCache = true
        )
    }

    private fun runAppBuildProperties(projectDir: File): TestResult {
        return gradlew(
            projectDir,
            ":app:generateAppBuildProperties",
            configurationCache = true
        )
    }
}
