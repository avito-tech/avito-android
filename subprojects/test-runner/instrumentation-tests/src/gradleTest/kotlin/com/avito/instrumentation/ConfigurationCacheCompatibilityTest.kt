package com.avito.instrumentation

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
    fun `configuration with applied plugin - reuses configuration cache`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id(instrumentationPluginId)
                    },
                    buildGradleExtra = instrumentationConfiguration(),
                    useKts = true,
                )
            )
        ).generateIn(projectDir)

        runHelp(projectDir).assertThat().buildSuccessful()

        runHelp(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    @Test
    fun `instrumentationTask run - reuses configuration cache`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id(instrumentationPluginId)
                    },
                    buildGradleExtra = instrumentationConfiguration(),
                    useKts = true,
                )
            )
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    /**
     * Proves the MBSA-2353 runId fix: with a ReportViewer (SendFromRunner) config the
     * task's runId derives from the git commit. A `git commit` between two builds must
     * NOT invalidate the configuration cache — the runId is read lazily from
     * GitInfoBuildService at task-graph time, so CC stores the recipe, not a baked value.
     */
    @Test
    fun `git commit between builds - instrumentation task CC reused`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id(instrumentationPluginId)
                    },
                    buildGradleExtra = instrumentationConfiguration(
                        report = """
                            |ReportConfig.ReportViewer.SendFromRunner(
                            |    reportApiUrl = "http://stub",
                            |    reportViewerUrl = "http://stub",
                            |    fileStorageUrl = "http://stub",
                            |    planSlug = "AvitoAndroid",
                            |    jobSlug = "FunctionalTests"
                            |)
                        """.trimMargin()
                    ),
                    useKts = true,
                )
            )
        ).generateIn(projectDir)

        val notes = File(projectDir, "notes.txt").apply { writeText("v1") }
        val git = Git.create(projectDir)
        git.init().getOrThrow()
        git.checkout(branchName = "feature", create = true).getOrThrow()
        git.addAll().getOrThrow()
        git.commit("initial").getOrThrow()

        runTask(projectDir).assertThat().buildSuccessful()

        notes.writeText("v2")
        git.commit("between builds").getOrThrow()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    /**
     * Guards the MBSA-2359 fix for the eager-args path: with SendFromDevice the report runId is
     * baked into AGP's `testInstrumentationRunnerArguments` at configuration time. It must be a
     * stable, git-independent value, so a `git commit` between two builds must NOT invalidate the
     * configuration cache. (Sibling guard to the SendFromRunner case above, which covers the lazy
     * task-graph path.)
     */
    @Test
    fun `git commit between builds - SendFromDevice CC reused`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id(instrumentationPluginId)
                    },
                    buildGradleExtra = instrumentationConfiguration(
                        report = """
                            |ReportConfig.ReportViewer.SendFromDevice(
                            |    reportApiUrl = "http://stub",
                            |    reportViewerUrl = "http://stub",
                            |    fileStorageUrl = "http://stub",
                            |    planSlug = "AvitoAndroid",
                            |    jobSlug = "FunctionalTests"
                            |)
                        """.trimMargin()
                    ),
                    useKts = true,
                )
            )
        ).generateIn(projectDir)

        val notes = File(projectDir, "notes.txt").apply { writeText("v1") }
        val git = Git.create(projectDir)
        git.init().getOrThrow()
        git.checkout(branchName = "feature", create = true).getOrThrow()
        git.addAll().getOrThrow()
        git.commit("initial").getOrThrow()

        runTask(projectDir).assertThat().buildSuccessful()

        notes.writeText("v2")
        git.commit("between builds").getOrThrow()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    private fun runHelp(projectDir: File): TestResult {
        return gradlew(
            projectDir,
            "help",
            "-PteamcityBuildId=0",
            "-PbuildNumber=100",
            "-PteamcityUrl=xxx",
            "-PgitBranch=xxx",
            "-PteamcityBuildType=BT",
            dryRun = true,
            configurationCache = true
        )
    }

    private fun runTask(projectDir: File): TestResult {
        return gradlew(
            projectDir,
            ":app:instrumentationFunctionalLocal",
            "-PteamcityBuildId=0",
            "-PbuildNumber=100",
            "-PteamcityUrl=xxx",
            "-PgitBranch=xxx",
            "-PteamcityBuildType=BT",
            "-PisGradleTestKitRun=true",
            dryRun = true,
            configurationCache = true
        )
    }
}
