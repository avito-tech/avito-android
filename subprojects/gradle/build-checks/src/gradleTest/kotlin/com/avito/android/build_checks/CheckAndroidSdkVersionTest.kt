package com.avito.android.build_checks

import com.avito.android.build_checks.BuildChecksTestProjectRunner.AndroidHomeLocation
import com.avito.android.build_checks.BuildChecksTestProjectRunner.AndroidHomeLocation.Custom
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class CheckAndroidSdkVersionTest {

    private var androidHome: File? = null
    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        val tmpDir = tempDir.toFile()
        androidHome = File(tmpDir, "android-sdk").apply { mkdirs() }
        projectDir = File(tmpDir, "project").apply { mkdirs() }
    }

    @Test
    fun `fail - no android sdk specified`() {
        androidHome?.delete()
        androidHome = null

        val result = runCheck(
            androidHomeLocation = AndroidHomeLocation.Absent,
            extension = """
                    compileSdkVersion = 29
                    revision = 5
                    """,
            expectFailure = true
        )
        result.assertThat()
            .buildFailed()
            .outputContains("Can't find ANDROID_HOME")
    }

    @Test
    fun `fail - no android sdk in specified path`() {
        androidHome?.delete()

        val result = runCheck(
            extension = """
                    compileSdkVersion = 29
                    revision = 5
                    """,
            expectFailure = true
        )
        result.assertThat()
            .buildFailed()
            .outputContains("ANDROID_HOME is not found")
    }

    @Test
    fun `fail - not specified versions`() {
        val result = runCheck(
            extension = """
                    // no versions
                    """,
            expectFailure = true
        )
        result.assertThat()
            .buildFailed()
            .outputContains("buildChecks.androidSdk.compileSdkVersion must be set")
    }

    @Test
    fun `fail - no SDK platform with specified version`() {
        givenAndroidSdkPlatform(version = 28, revision = 1)

        val result = runCheck(
            extension = """
                    compileSdkVersion = 29
                    revision = 5
                    """,
            expectFailure = true
        )
        result.assertThat()
            .buildFailed()
            .outputContains("Android SDK platform 29 is not found")
    }

    @Test
    fun `fail - an old platform revision`() {
        givenAndroidSdkPlatform(version = 29, revision = 4)

        val result = runCheck(
            extension = """
                    compileSdkVersion = 29
                    revision = 5
                    """,
            expectFailure = true
        )
        result.assertThat()
            .buildFailed()
            .outputContains(
                "You have an old Android SDK Platform version.\n" +
                    "API level: 29, \n(actual revision 4, expected revision: 5)"
            )
    }

    @Test
    fun `success - the same platform revision`() {
        givenAndroidSdkPlatform(version = 29, revision = 5)

        val result = runCheck(
            extension = """
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
            extension = """
                    compileSdkVersion = 29
                    revision = 5
                    """
        )
        result.assertThat().buildSuccessful()
            .outputContains(
                "You have a newer Android SDK Platform version.\n" +
                    "API level: 29, \n(actual revision 6, expected revision: 5)"
            )
    }

    private fun runCheck(
        androidHomeLocation: AndroidHomeLocation = Custom(requireNotNull(androidHome)),
        extension: String,
        expectFailure: Boolean = false
    ): TestResult {
        return BuildChecksTestProjectRunner(
            projectDir = projectDir,
            androidHome = androidHomeLocation,
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
        requireNotNull(androidHome)
            .dir("platforms")
            .dir("android-$version")
            .file("source.properties", """Pkg.Revision=$revision""".trimIndent())
    }
}
