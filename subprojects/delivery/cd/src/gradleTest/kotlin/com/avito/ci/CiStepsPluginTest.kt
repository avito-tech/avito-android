package com.avito.ci

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

internal class CiStepsPluginTest : BaseCiStepsPluginTest() {

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()
        generateProjectWithConfiguredCiSteps()
    }

    @TestFactory
    fun `assemble should not trigger artifacts tasks`(): List<DynamicTest> {
        val result = runTask(":appA:assemble")

        return listOf(
            ":appA:releaseCopyArtifacts",
            ":appA:releaseVerifyArtifacts"
        ).map { task ->
            dynamicTest("$task should not be triggered on dev build, it's CI only") {
                result.assertThat().tasksShouldNotBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should trigger project release packageRelease task`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:packageRelease"
        ).map { task ->
            dynamicTest("$task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should not trigger package task for unnecessary build types`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:packageStaging"
        ).map { task ->
            dynamicTest("$task should not be triggered by :appA:release") {
                result.assertThat().tasksShouldNotBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should trigger artifacts tasks`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:releaseCopyArtifacts",
            ":appA:releaseVerifyArtifacts"
        ).map { task ->
            dynamicTest("$task should be triggered by release tasks") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should not trigger sibling project tasks`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appB",
            ":independent"
        ).map { module ->
            dynamicTest("$module should not be triggered by :appA:release") {
                result.assertThat().moduleTaskShouldNotBeTriggered(module)
            }
        }
    }

    @TestFactory
    fun `release should trigger project lint task`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:lintRelease"
        ).map { task ->
            dynamicTest("$task should be triggered by :appA:release") {
                result.assertThat().moduleTaskShouldNotBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should run test for all dependant modules`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:test",
            ":shared:test",
            ":transitive:test"
        ).map { task ->
            dynamicTest("$task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should trigger project debug package tasks as it is necessary for ui tests`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:packageDebug",
            ":appA:packageDebugAndroidTest"
        ).map { task ->
            dynamicTest("$task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should specified ui test configurations`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:instrumentationRegressDefault"
        ).map { task ->
            dynamicTest("$task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release should not trigger all ui test configurations`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:instrumentationPrDebug"
        ).map { task ->
            dynamicTest("$task should NOT be triggered by :appA:release") {
                result.assertThat().tasksShouldNotBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release builds and signs bundle`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:packageReleaseBundle",
            ":appA:legacySignBundleViaServiceRelease"
        ).map { task ->
            dynamicTest("$task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release signs release apk via service`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:legacySignApkViaServiceRelease"
        ).map { task ->
            dynamicTest("$task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release does not sign apk for unnecessary build types`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:signApkViaServiceStaging"
        ).map { task ->
            dynamicTest("$task should not be triggered by :appA:release") {
                result.assertThat().tasksShouldNotBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release uploads debug apk to prosector`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:prosectorUploadDebug"
        ).map { task ->
            dynamicTest("$task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release uploads release apk to qapps`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:qappsUploadRelease"
        ).map { task ->
            dynamicTest("$task should be triggered by :appA:release") {
                result.assertThat().tasksShouldBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `release does not upload apk for unnecessary build types`(): List<DynamicTest> {
        val result = runTask(":appA:release")

        return listOf(
            ":appA:qappsUploadDebug",
            ":appA:qappsUploadStaging"
        ).map { task ->
            dynamicTest("$task should NOT be triggered by :appA:release") {
                result.assertThat().tasksShouldNotBeTriggered(task)
            }
        }
    }

    @TestFactory
    fun `packageDebug should not trigger CI tasks`(): List<DynamicTest> {
        val result = runTask(":appA:packageDebug")

        return listOf(
            ":appA:signViaService"
        ).map { task ->
            dynamicTest("$task should not be triggered on dev build, it's CI only") {
                result.assertThat().tasksShouldNotBeTriggered(task)
            }
        }
    }

    @Test
    fun `file is missed - verify artifacts fails`() {
        val result = runTask(":appA:releaseVerifyArtifacts", dryRun = false, expectedFailure = true)

        result.assertThat().run {
            buildFailed().outputContains("Artifact: appA/build/reports/not-existed-file.json not found")
        }
    }
}
