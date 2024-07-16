package com.avito.test.summary

import com.avito.test.gradle.TestResult
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationCacheCompatibilityTest {

    @Test
    fun `testSummary task - ok`(@TempDir projectDir: File) {
        MinimalTestSummaryProject.builder(projectDir.file("test_summary_destination.json")).generateIn(projectDir)

        val testSummaryTask = testSummaryTaskName(MinimalTestSummaryProject.appName)

        runTask(projectDir, testSummaryTask).assertThat().buildSuccessful()

        runTask(projectDir, testSummaryTask).assertThat().buildSuccessful().configurationCachedReused()
    }

    @Test
    fun `markReportForTms task - ok`(@TempDir projectDir: File) {
        MinimalTestSummaryProject.builder(projectDir.file("test_summary_destination.json")).generateIn(projectDir)

        val testSummaryTask = markReportForTmsTaskName(MinimalTestSummaryProject.appName)

        runTask(projectDir, testSummaryTask).assertThat().buildSuccessful()

        runTask(projectDir, testSummaryTask).assertThat().buildSuccessful().configurationCachedReused()
    }

    private fun runTask(projectDir: File, task: String): TestResult {
        return gradlew(
            projectDir,
            task,
            dryRun = true,
            configurationCache = true
        )
    }
}
