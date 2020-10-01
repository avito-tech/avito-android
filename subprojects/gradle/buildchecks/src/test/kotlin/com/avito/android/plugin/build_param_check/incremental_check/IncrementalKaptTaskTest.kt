package com.avito.android.plugin.build_param_check.incremental_check

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class IncrementalKaptTaskTest {

    private lateinit var projectDir: File

    @BeforeEach
    internal fun setUp(@TempDir projectDir: File) {
        this.projectDir = projectDir
    }

    @Test
    fun `build success with no warnings - unsupported Java version`() {
        generateProject(mode = "none")

        checkIncrementalKapt(javaVersion = UNSUPPORTED_JAVA_VERSION)
            .assertThat()
            .buildSuccessful()
            .outputDoesNotContain(
                substring = ERROR_MESSAGE_FIRST_LINE
            )
    }

    @Disabled("Can't change java version in tests, see commit message [MBS-9506]")
    @Test
    fun `build success with warning - unsupported Java version`() {
        generateProject(mode = "warning")

        checkIncrementalKapt(javaVersion = UNSUPPORTED_JAVA_VERSION)
            .assertThat()
            .buildSuccessful()
            .outputContains(
                substring = ERROR_MESSAGE_FIRST_LINE
            )
    }

    @Disabled("Can't change java version in tests, see commit message [MBS-9506]")
    @Test
    fun `build fail - unsupported Java version`() {
        generateProject(mode = "fail")

        checkIncrementalKapt(javaVersion = UNSUPPORTED_JAVA_VERSION, expectFailure = true)
            .assertThat()
            .buildFailed(
                expectedErrorSubstring = ERROR_MESSAGE_FIRST_LINE
            )
    }

    @Test
    fun `build success - supported Java version`() {
        generateProject(mode = "fail")

        checkIncrementalKapt(javaVersion = SUPPORTED_JAVA_VERSION)
            .assertThat()
            .buildSuccessful()
            .outputDoesNotContain(
                substring = ERROR_MESSAGE_FIRST_LINE
            )
    }

    @Test
    fun `build success - Room not applied`() {
        generateProject(mode = "fail", applyRoomPlugin = false)

        checkIncrementalKapt(javaVersion = SUPPORTED_JAVA_VERSION)
            .assertThat()
            .buildSuccessful()
            .outputDoesNotContain(
                substring = ERROR_MESSAGE_FIRST_LINE
            )
    }

    @Test
    fun `build success - without Room and unsupported Java`() {
        generateProject(mode = "fail", applyRoomPlugin = false)

        checkIncrementalKapt(javaVersion = UNSUPPORTED_JAVA_VERSION)
            .assertThat()
            .buildSuccessful()
            .outputDoesNotContain(
                substring = ERROR_MESSAGE_FIRST_LINE
            )
    }

    private fun checkIncrementalKapt(
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

    private fun generateProject(
        mode: String,
        applyRoomPlugin: Boolean = true
    ) {
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
                    plugins = listOfNotNull(
                        "kotlin-kapt",
                        if (applyRoomPlugin) "com.avito.android.room-config" else null
                    )
                )
            )
        ).generateIn(projectDir)
    }
}

private const val ERROR_MESSAGE_FIRST_LINE =
    "Incremental KAPT is turned on (kapt.incremental.apt=true) but Room does not support it in current conditions"

// About Java version read here - https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-master-dev/room/compiler/src/main/kotlin/androidx/room/RoomProcessor.kt
private const val SUPPORTED_JAVA_VERSION = "11.0.0"
private const val UNSUPPORTED_JAVA_VERSION = "1.8.0_202"
