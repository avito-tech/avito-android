package com.avito.plugin

import com.avito.http.HttpCodes
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.MockWebServerFactory
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class LegacySignServicePluginTest {

    @TempDir
    lateinit var testProjectDir: File

    private val mockWebServer = MockWebServerFactory.create()

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `plugin apply - fails - configuration with invalid url`() {
        generateTestProject(
            signServiceExtension = configureExtension(url = "some_incorrect_url")
        )

        ciRun(
            testProjectDir,
            ":app:legacySignApkViaServiceRelease",
            "-PsignToken=12345",
            dryRun = true,
            expectFailure = true
        ).assertThat()
            .buildFailed()
            .outputContains("Invalid signer url value: 'some_incorrect_url'")
    }

    @Test
    fun `apk signing - fails - without required params, sign tasks in graph`() {
        generateTestProject()

        ciRun(
            testProjectDir,
            ":app:legacySignApkViaServiceRelease",
            expectFailure = true
        ).assertThat()
            .buildFailed()
            .outputContains("Can't sign variant: 'release'; token is not set")
    }

    @Test
    fun `apk signing - skipped - sign tasks in graph, variant not registered, no token`() {
        generateTestProject()

        ciRun(
            testProjectDir,
            ":app:legacySignApkViaServiceDebug", // mind that only release is registered in extension
        ).assertThat().buildSuccessful()
    }

    @Test
    fun `apk signing - skipped - without required params, sign task not called`() {
        generateTestProject()

        gradlew(
            testProjectDir,
            ":app:assembleRelease",
        ).assertThat().buildSuccessful()
    }

    @Test
    fun `apk signing - skipped - token set, sign task in graph, signing disabled`() {
        generateTestProject(
            signServiceExtension = configureExtension(enabled = false)
        )

        gradlew(
            testProjectDir,
            ":app:legacySignApkViaServiceRelease",
            "-PsignToken=12345"
        ).assertThat().buildSuccessful()
    }

    @Test
    fun `apk signing - skipped - token not set, sign task in graph, signing disabled`() {
        generateTestProject(
            signServiceExtension = configureExtension(enabled = false)
        )

        gradlew(
            testProjectDir,
            ":app:legacySignApkViaServiceRelease",
        ).assertThat().buildSuccessful()
    }

    /**
     * TODO: check whether this contract actual or not for a service or CI outputs
     */
    @Test
    fun `bundle path check`() {
        generateTestProject()
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpCodes.OK))

        val result = gradlew(
            testProjectDir,
            ":app:legacySignBundleViaServiceRelease",
            "-PsignToken=12345"
        )

        result.assertThat().buildSuccessful()

        val request = mockWebServer.takeRequest()

        val body = request.body.readString(Charsets.UTF_8)

        assertThat(body).contains("filename=\"app-release.aab\"")
    }

    @Test
    fun `apk signing task - runs after packaging`() {
        generateTestProject()

        gradlew(
            testProjectDir,
            ":app:legacySignApkViaServiceRelease",
            "-PsignToken=12345",
            dryRun = true
        ).assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(
                ":app:packageRelease",
                ":app:legacySignApkViaServiceRelease"
            ).inOrder()
    }

    @Test
    fun `apk signing task - adds signed version to outputs`() {
        generateTestProject()
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpCodes.OK).setBody("SIGNED_CONTENT"))

        gradlew(
            testProjectDir,
            ":app:legacySignApkViaServiceRelease",
            "-PsignToken=12345"
        ).assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(":app:packageRelease")

        val unsignedApk = File(testProjectDir, "app/build/outputs/apk/release/app-release-unsigned.apk")
        assertWithMessage("Preserve original unsigned APK").that(unsignedApk.exists()).isTrue()

        val signedApk = File(testProjectDir, "app/build/outputs/apk/release/app-release.apk")
        assertWithMessage("Copy signed APK to outputs. See explanation for this hack inside SignTask")
            .that(signedApk.exists()).isTrue()
        assertThat(signedApk.readText()).isEqualTo("SIGNED_CONTENT")
    }

    @Test
    fun `bundle signing task - replaces original output by signed version (HACK)`() {
        generateTestProject()
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpCodes.OK).setBody("SIGNED_CONTENT"))

        gradlew(
            testProjectDir,
            ":app:legacySignBundleViaServiceRelease",
            "-PsignToken=12345"
        ).assertThat().buildSuccessful()

        // See explanation for this hack inside SignTask
        val resultArtifact = File(testProjectDir, "app/build/outputs/bundle/release/app-release.aab")
        assertThat(resultArtifact.exists()).isTrue()
        assertThat(resultArtifact.readText()).isEqualTo("SIGNED_CONTENT")
    }

    @Test
    fun `apk signing task - failed - service error`() {
        generateTestProject()
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpCodes.NOT_FOUND))

        gradlew(
            testProjectDir,
            ":app:legacySignApkViaServiceRelease",
            "-PsignToken=12345",
            expectFailure = true
        ).assertThat().buildFailed()
            .outputContains("Can't sign")
            .outputContains("404")
            .outputContains("${mockWebServer.url("/sign")}")
            .outputContains("Response body is empty")
            .outputContains("Content-Length: 0")
            .outputContains("Request body size: ")
    }

    private fun generateTestProject(
        signServiceExtension: String = configureExtension(),
    ) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    "app",
                    enableKotlinAndroidPlugin = false,
                    versionCode = 100,
                    versionName = "22.1",
                    plugins = plugins {
                        id("com.avito.android.signer")
                    },
                    buildGradleExtra = signServiceExtension
                )
            ),
        ).generateIn(testProjectDir)
    }

    private fun configureExtension(
        enabled: Boolean = true,
        url: String = "${mockWebServer.url("/")}"
    ) = """
         signService {
            enabled = $enabled
            url = "$url"
            apk(android.buildTypes.release, project.properties.get("signToken"))
            bundle(android.buildTypes.release, project.properties.get("signToken"))
         }
    """.trimIndent()
}
