package com.avito.tech_budget.detekt

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DetektConfigurationTest {

    @Test
    fun `when detekt configuration is not applied - run detekt on release variant - fail with task not found`(
        @TempDir projectDir: File
    ) {
        generateAndroidProject(projectDir, detektEnabled = false)

        val result = gradlew(
            projectDir,
            ":app:detektTechBudgetRelease",
            "-Pcom.avito.android.tech-budget.enable=true",
            expectFailure = true
        )

        result.assertThat()
            .buildFailed()
            .outputContains("Cannot locate tasks that match ':app:detektTechBudgetRelease'")
    }

    @Test
    fun `when detekt configuration is applied - run detekt on release variant - run task in correct order`(
        @TempDir projectDir: File
    ) {
        generateAndroidProject(projectDir)

        val result = gradlew(
            projectDir,
            ":app:detektTechBudgetRelease",
            "-Pcom.avito.android.tech-budget.enable=true",
        )

        result.assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(
                ":app:compileReleaseKotlin",
                ":app:detektTechBudgetRelease"
            )
            .inOrder()
    }

    @Test
    fun `when detekt configuration is applied - run detekt on unit test release variant - run task in correct order`(
        @TempDir projectDir: File
    ) {
        generateAndroidProject(projectDir)

        val result = gradlew(
            projectDir,
            ":app:detektTechBudgetReleaseUnitTest",
            "-Pcom.avito.android.tech-budget.enable=true",
        )

        result.assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(
                ":app:compileReleaseKotlin",
                ":app:compileReleaseUnitTestKotlin",
                ":app:detektTechBudgetReleaseUnitTest"
            )
            .inOrder()
    }

    private fun generateAndroidProject(
        projectDir: File,
        detektEnabled: Boolean = true
    ) = TestProjectGenerator(
        plugins = plugins {
            id("com.avito.android.tech-budget")
        },
        useKts = true,
        buildGradleExtra = """
             techBudget {
                 detekt {
                     enabled.set($detektEnabled)
                     configFiles.from(file("detekt-tech-budget.yaml"))
                 }
             }
        """.trimIndent(),
        modules = listOf(
            AndroidAppModule(
                name = "app",
                plugins = plugins {
                    id("com.avito.android.tech-budget")
                    id("io.gitlab.arturbosch.detekt")
                },
                useKts = true,
            )
        ),

    ).generateIn(projectDir).also {
        projectDir.file("detekt-tech-budget.yaml", MINIMAL_DETEKT_CONFIG_FILE)
    }
}

@Language("yaml")
internal val MINIMAL_DETEKT_CONFIG_FILE = """
    build:
        maxIssues: 0
        excludeCorrectable: false
        weights:

    config:
        validation: true
        warningsAsErrors: false
        excludes: ''
""".trimIndent()
