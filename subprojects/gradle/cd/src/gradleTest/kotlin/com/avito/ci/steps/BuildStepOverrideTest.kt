package com.avito.ci.steps

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class BuildStepOverrideTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()
    }

    @Test
    fun `register with the same name - fails`() {
        generateProject(
            """
            customTask("step") {
                tasksPredicate = com.avito.ci.TasksPredicate.byName("tasks")
            }
            customTask("step") {
                tasksPredicate = com.avito.ci.TasksPredicate.byName("help")
            }
        """.trimIndent()
        )

        val buildResult = runBuild(
            expectFailure = true
        )

        buildResult.assertThat()
            .buildFailed()
            .outputContains("Overriding existing build step 'step'")
    }

    @Test
    fun `override by type - overrides previous one`() {
        generateProject(
            """
            customTask("step") {
                tasksPredicate = com.avito.ci.TasksPredicate.byName("tasks")
            }
            overrideStep(com.avito.ci.steps.CustomTaskStep.class) {
                tasksPredicate = com.avito.ci.TasksPredicate.byName("help")
            }
        """.trimIndent()
        )

        val buildResult = runBuild()

        buildResult.assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(":app:help", ":app:release")

        buildResult.assertThat()
            .tasksShouldNotBeTriggered(":app:tasks")
    }

    @Test
    fun `override by type - fail - multiple steps`() {
        generateProject(
            """
            customTask("tasks") {
                tasksPredicate = com.avito.ci.TasksPredicate.byName("tasks")
            }
            customTask("help") {
                tasksPredicate = com.avito.ci.TasksPredicate.byName("help")
            }
            overrideStep(com.avito.ci.steps.CustomTaskStep.class) {
                tasksPredicate = com.avito.ci.TasksPredicate.byName("help")
            }
            """.trimIndent()
        )

        val buildResult = runBuild(
            expectFailure = true
        )

        buildResult.assertThat()
            .buildFailed()
    }

    @Test
    fun `override by name - overrides previous one`() {
        generateProject(
            """
            customTask("tasks") {
                tasksPredicate = com.avito.ci.TasksPredicate.byName("tasks")
            }
            customTask("help") {
                tasksPredicate = com.avito.ci.TasksPredicate.byName("help")
            }
            overrideStep("help", com.avito.ci.steps.CustomTaskStep.class) {
                tasksPredicate = com.avito.ci.TasksPredicate.byName("tasks")
            }
            """.trimIndent()
        )

        val buildResult = runBuild()

        buildResult.assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(":app:tasks", ":app:release")

        buildResult.assertThat()
            .tasksShouldNotBeTriggered(":app:help")
    }

    private fun runBuild(
        expectFailure: Boolean = false
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
            modules = listOf(
                AndroidAppModule(
                    name = "app",
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
                )
            )
        ).generateIn(projectDir)
    }
}
