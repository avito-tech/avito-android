package com.avito.ci.steps

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class CustomTaskStepTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()
    }

    @Test
    fun `customTask - executes task - by name`() {
        generateProject(
            """
            customTask("tasks") {
                tasksPredicate = com.avito.ci.TasksPredicate.byName("tasks")
            }
        """.trimIndent()
        )

        val buildResult = runBuild()

        buildResult.assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(":tasks", ":release")
    }

    @Test
    fun `customTask - executes task - by type`() {
        generateProject(
            """
            customTask("tasks") {
                def type = org.gradle.api.tasks.diagnostics.TaskReportTask.class
                tasksPredicate = com.avito.ci.TasksPredicate.byType(type)
            }
        """.trimIndent()
        )

        val buildResult = runBuild()

        buildResult.assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(":tasks", ":release")
    }

    @Test
    fun `customTask - executes tasks - multiple steps`() {
        generateProject(
            """
            customTask("tasks") {
                tasksPredicate = com.avito.ci.TasksPredicate.byName("tasks")
            }
            customTask("help") {
                tasksPredicate = com.avito.ci.TasksPredicate.byName("help")
            }
        """.trimIndent()
        )

        val buildResult = runBuild()

        buildResult.assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(":tasks", ":help", ":release")
    }

    private fun runBuild(
        expectFailure: Boolean = false,
    ): TestResult {
        return gradlew(
            projectDir,
            "release",
            "-Pci=true",
            expectFailure = expectFailure
        )
    }

    private fun generateProject(stepsDeclaration: String) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.cd")
            },
            buildGradleExtra = """
                builds {
                    release {
                          $stepsDeclaration
                    }
                }
            """.trimIndent()
        ).generateIn(projectDir)
    }
}
