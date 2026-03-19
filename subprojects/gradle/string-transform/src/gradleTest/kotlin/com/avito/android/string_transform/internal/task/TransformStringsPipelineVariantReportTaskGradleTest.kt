package com.avito.android.string_transform.internal.task

import com.google.common.truth.Truth.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class TransformStringsPipelineVariantReportTaskGradleTest {

    private lateinit var project: Project

    @BeforeEach
    fun setUp(@TempDir dir: File) {
        project = ProjectBuilder.builder()
            .withProjectDir(dir)
            .build()
    }

    @Test
    fun `report task - writes failure report and rethrows - when stub processing fails`() {
        val task = project.tasks.register(
            "transformStringsAlphaRelease",
            FailingTransformStringsPipelineVariantReportTask::class.java,
        ).get()

        with(task) {
            modulePath.set(":app")
            pipelineName.set("alpha")
            variantName.set("release")
            totalRules.set(1)
            exactRuleCount.set(1)
            caseExpandedRuleCount.set(0)
            configurationWarnings.set(emptyList())
            reportFile.set(
                project.layout.buildDirectory.file(
                    "outputs/transformStrings/alpha/release/report/transform-report.json"
                )
            )
        }

        val error = assertThrows(IllegalStateException::class.java) {
            task.writeReport()
        }

        val report = project.layout.buildDirectory
            .file("outputs/transformStrings/alpha/release/report/transform-report.json")
            .get()
            .asFile

        assertThat(error).hasMessageThat().isEqualTo("boom")
        assertThat(report.exists()).isTrue()

        val reportText = report.readText()
        assertThat(reportText).contains("\"status\": \"FAILURE\"")
        assertThat(reportText).contains("\"severity\": \"HARD_FAILURE\"")
        assertThat(reportText).contains("\"message\": \"boom\"")
    }

    internal abstract class FailingTransformStringsPipelineVariantReportTask :
        TransformStringsPipelineVariantReportTask() {

        override fun runStubProcessing() {
            error("boom")
        }
    }
}
