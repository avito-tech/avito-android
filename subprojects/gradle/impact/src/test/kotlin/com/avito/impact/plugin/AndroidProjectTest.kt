package com.avito.impact.plugin

import com.avito.impact.util.AndroidManifest
import com.avito.impact.util.AndroidProject
import com.avito.impact.util.R
import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.AndroidLibModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.google.common.truth.isInstanceOf
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class AndroidProjectTest {

    private lateinit var tempDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        this.tempDir = tempDir
    }

    @Test
    fun `android project - r files`() {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app", packageName = "com.app", dependencies = """
                    implementation project(":lib")
                """.trimIndent()
                ),
                AndroidLibModule(name = "lib", packageName = "com.lib")
            )
        ).generateIn(tempDir)

        val buildResult = build(":app:assembleAndroidTest")
        assertThat(buildResult).isInstanceOf<TestResult.Success>()

        val projectStub = applicationProjectStub(projectDir = File(tempDir, "app"))
        val androidProject = AndroidProject(projectStub)

        assertThat(androidProject.debug.manifest.getPackage()).isEqualTo("com.app")
        val rFiles = androidProject.debug.resourceSymbolList
        assertWithMessage("R file for application").that(rFiles).isNotNull()
    }

    @Test
    fun `android manifest - package`() {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(name = "app", packageName = "com.app")
            )
        ).generateIn(tempDir)

        val projectDir = File(tempDir, "app")
        val manifest = AndroidManifest(projectDir)

        assertThat(manifest.getPackage()).isEqualTo("com.app")
    }

    private fun applicationProjectStub(projectDir: File): Project {
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir)
            .build()
        project.pluginManager.apply("com.android.application")
        return project
    }

    private fun build(vararg tasks: String): TestResult {
        return gradlew(tempDir, *tasks)
    }

}
