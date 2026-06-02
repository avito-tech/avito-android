package com.avito.android.info

import com.avito.git.Git
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

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
