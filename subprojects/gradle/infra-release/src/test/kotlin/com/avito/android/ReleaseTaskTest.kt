package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.git
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ReleaseTaskTest {

    @Test
    fun test(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.infra-release"),
            buildGradleExtra = """
                infraRelease {
                    releaseTag = "2020.26"
                    previousReleaseTag = "2020.25"
                }
            """.trimIndent()
        ).generateIn(projectDir)

        with(projectDir) {
            git("init --quiet")
            git("checkout -b 2020.26")
        }

        gradlew(
            projectDir,
            "infraRelease",
            "-Pavito.git.state=local"
        ).assertThat().buildSuccessful()
    }
}
