package com.avito.android.plugin.build_param_check

import com.avito.test.gradle.TestResult
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class CheckAndroidSdkVersionTest {

    private lateinit var androidHome: File
    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        val tmpDir = tempDir.toFile()
        androidHome = File(tmpDir, "android-sdk").apply { mkdirs() }
        projectDir = File(tmpDir, "project").apply { mkdirs() }
    }

    @Test
    fun `fail - no android sdk`() {
        androidHome.delete()

        val result = runCheck(
            """
                    compileSdkVersion = 29
                    revision = 5
                    """,
            expectFailure = true
        )
        result.assertThat()
            .buildFailed("ANDROID_HOME is not found")
    }

    @Test
    fun `fail - not specified versions`() {
        val result = runCheck(
            """
                    // no versions
                    """,
            expectFailure = true
        )
        result.assertThat()
            .buildFailed("buildChecks.androidSdk.compileSdkVersion must be set")
    }

    @Test
    fun `fail - no SDK platform with specified version`() {
        givenAndroidSdkPlatform(version = 28, revision = 1)

        val result = runCheck(
            """
                    compileSdkVersion = 29
                    revision = 5
                    """,
            expectFailure = true
        )
        result.assertThat()
            .buildFailed(
                "Android SDK platform 29 is not found"
            )
    }

    @Test
    fun `fail - an old platform revision`() {
        givenAndroidSdkPlatform(version = 29, revision = 4)

        val result = runCheck(
            """
                    compileSdkVersion = 29
                    revision = 5
                    """,
            expectFailure = true
        )
        result.assertThat()
            .buildFailed(
                "You have an old Android SDK Platform version.\n" +
                    "API level: 29, actual revision 4, expected revision: 5"
            )
    }

    @Test
    fun `success - the same platform revision`() {
        givenAndroidSdkPlatform(version = 29, revision = 5)

        val result = runCheck(
            """
                    compileSdkVersion = 29
                    revision = 5
                    """
        )
        result.assertThat().buildSuccessful()
    }

    @Test
    fun `warning - a newer platform revision`() {
        givenAndroidSdkPlatform(version = 29, revision = 6)

        val result = runCheck(
            """
                    compileSdkVersion = 29
                    revision = 5
                    """
        )
        result.assertThat().buildSuccessful()
            .outputContains(
                "You have a newer Android SDK Platform version.\n" +
                    "API level: 29, actual revision 6, expected revision: 5"
            )
    }

    private fun runCheck(extension: String, expectFailure: Boolean = false): TestResult {
        return BuildChecksTestProjectRunner(
            projectDir, androidHome,
            buildChecksExtension = """
                enableByDefault = false
                androidSdk { 
                    enabled = true
                    $extension
                }
            """
        ).runChecks(expectFailure)
    }

    private fun givenAndroidSdkPlatform(version: Int, revision: Int) {
        androidHome
            .dir("platforms")
            .dir("android-$version")
            .file(
                "source.properties", """
                    Pkg.Revision=$revision
                """.trimIndent()
            )
    }
}
