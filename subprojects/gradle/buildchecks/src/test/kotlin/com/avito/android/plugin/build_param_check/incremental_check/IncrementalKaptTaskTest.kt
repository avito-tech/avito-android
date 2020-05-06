package com.avito.android.plugin.build_param_check.incremental_check

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class IncrementalKaptTaskTest {

    @Test
    fun `build success with no warnings - unsupported Java version`(@TempDir projectDir: File) {
        generateProject("none", projectDir)

        checkIncrementalKapt(projectDir = projectDir, javaVersion = UNSUPPORTED_JAVA_VERSION)
            .assertThat()
            .buildSuccessful()
            .outputDoesNotContain(
                substring = "Incremental KAPT is turned on (kapt.incremental.apt=true) but Room does not support it in current conditions"
            )
    }

    @Test
    fun `build success with warning - unsupported Java version`(@TempDir projectDir: File) {
        generateProject("warning", projectDir)

        checkIncrementalKapt(projectDir = projectDir, javaVersion = UNSUPPORTED_JAVA_VERSION)
            .assertThat()
            .buildSuccessful()
            .outputContains(
                substring = "Incremental KAPT is turned on (kapt.incremental.apt=true) but Room does not support it in current conditions"
            )
    }

    @Test
    fun `build fail - unsupported Java version`(@TempDir projectDir: File) {
        generateProject("fail", projectDir)

        checkIncrementalKapt(projectDir = projectDir, javaVersion = UNSUPPORTED_JAVA_VERSION, expectFailure = true)
            .assertThat()
            .buildFailed(
                expectedErrorSubstring = "Incremental KAPT is turned on (kapt.incremental.apt=true) but Room does not support it in current conditions"
            )
    }

    @Test
    fun `build success - supported Java version`(@TempDir projectDir: File) {
        generateProject("fail", projectDir)

        checkIncrementalKapt(projectDir = projectDir, javaVersion = SUPPORTED_JAVA_VERSION)
            .assertThat()
            .buildSuccessful()
            .outputDoesNotContain(
                substring = "Incremental KAPT is turned on (kapt.incremental.apt=true) but Room does not support it in current conditions"
            )
    }

    private fun checkIncrementalKapt(
        projectDir: File,
        javaVersion: String,
        expectFailure: Boolean = false
    ) = gradlew(
        projectDir,
        ":checkIncrementalKapt",
        "-ParchPersistenceVersion=2.2.4",
        "-Pkapt.incremental.apt=true",
        "-Djava.runtime.version=$javaVersion",
        "-Djava.vendor=Avito",
        expectFailure = expectFailure
    )

    private fun generateProject(mode: String, projectDir: File) {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.buildchecks"),
            buildGradleExtra = """
                buildChecks {
                    enableByDefault = false
                    incrementalKapt {
                        mode = "$mode"
                    }
                }
            """.trimIndent(),
            modules = listOf(
                AndroidAppModule(
                    name = "room-test",
                    plugins = listOf("kotlin-android", "kotlin-kapt", "com.avito.android.room-config")
                )
            )
        ).generateIn(projectDir)
    }
}

// About Java version read here - https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-master-dev/room/compiler/src/main/kotlin/androidx/room/RoomProcessor.kt
private const val SUPPORTED_JAVA_VERSION = "11.0.0"
private const val UNSUPPORTED_JAVA_VERSION = "1.8.0_202"
