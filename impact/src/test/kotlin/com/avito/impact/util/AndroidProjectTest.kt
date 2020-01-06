package com.avito.impact.util

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.AndroidLibModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.google.common.truth.Truth.assertThat
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
    fun `android project - r files`(){
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(name = "app", packageName = "com.app", dependencies = """
                    implementation project(":lib")
                """.trimIndent()),
                AndroidLibModule(name = "lib", packageName = "com.lib")
            )
        ).generateIn(tempDir)

        val buildResult = build(":app:assembleAndroidTest")
        assertThat(buildResult).isInstanceOf<TestResult.Success>()

        val projectStub = applicationProjectStub(projectDir = File(tempDir, "app"))
        val androidProject = AndroidProject(projectStub)

        assertThat(androidProject.debug.manifest.getPackage()).isEqualTo("com.app")
        val rFiles = androidProject.debug.rs
        val appR = rFiles.firstOrNull { r -> r.getPackage() == "com.app" }
        val libR = rFiles.firstOrNull { r -> r.getPackage() == "com.lib" }
        assertThat(appR).isNotNull()
        assertThat(libR).isNotNull()
    }

    @Test
    fun `android manifest - package`(){
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(name = "app", packageName = "com.app")
            )
        ).generateIn(tempDir)

        val projectDir = File(tempDir, "app")
        val manifest = AndroidManifest(projectDir)

        assertThat(manifest.getPackage()).isEqualTo("com.app")
    }

    @Test
    fun `R file - content`(){
        val rFile = File(tempDir, "R.java")
        rFile.writeText("""
        /* AUTO-GENERATED FILE.  DO NOT MODIFY */
        package com.app;
        
        public final class R {
            private R() {}
        
            public static final class drawable {
            }
            public static final class id {
                private id() {}
        
                public static final int root = 0x7f0a002b;
            }
        }
        """.trimIndent())

        val r = R(rFile)

        assertThat(r.getPackage()).isEqualTo("com.app")
        assertThat(r.contains(2131361835)).isTrue()
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
