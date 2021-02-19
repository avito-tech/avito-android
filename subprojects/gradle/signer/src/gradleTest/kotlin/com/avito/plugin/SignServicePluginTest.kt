package com.avito.plugin

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

class SignServicePluginTest {

    @TempDir
    lateinit var testProjectDir: File

    private val mockWebServer = MockWebServerFactory.create()

    private val defaultExtension = """
         signService {
            host = "${mockWebServer.url("/")}"
            apk(android.buildTypes.release, project.properties.get("signToken"))
            bundle(android.buildTypes.release, project.properties.get("signToken"))
         }
    """.trimIndent()

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `plugin apply - fails - configuration without host`() {
        generateTestProject(
            signServiceExtension = """
             signService {
                // host
                apk(android.buildTypes.release, "signToken")
                bundle(android.buildTypes.release, "signToken")
             }
        """.trimIndent()
        )

        val result = ciRun(
            testProjectDir,
            ":app:signApkViaServiceRelease",
            dryRun = true,
            expectFailure = true
        )

        result.assertThat().buildFailed("host")
    }

    @Test
    fun `apk signing fails - without required params - when sign tasks in graph (on ci)`() {
        generateTestProject()

        val result = ciRun(
            testProjectDir,
            ":app:signApkViaServiceRelease",
            expectFailure = true
        )

        result.assertThat().buildFailed("can't sign")
    }

    @Test
    fun `apk signing skipped - without required params - with allowSkip`() {
        generateTestProject()

        val result = gradlew(
            testProjectDir,
            ":app:signApkViaServiceRelease",
            "-Pavito.signer.allowSkip=true"
        )

        result.assertThat().buildSuccessful()
    }

    /**
     * TODO: check whether this contract actual or not for a service or CI outputs
     */
    @Test
    fun `bundle path check`() {
        generateTestProject()
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val result = gradlew(
            testProjectDir,
            ":app:signBundleViaServiceRelease",
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

        val result = gradlew(
            testProjectDir,
            ":app:signApkViaServiceRelease",
            "-PsignToken=12345",
            dryRun = true
        )

        result.assertThat().tasksShouldBeTriggered(
            ":app:packageRelease",
            ":app:signApkViaServiceRelease"
        ).inOrder()
    }

    @Test
    fun `apk signing task - adds signed version to outputs`() {
        generateTestProject()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("SIGNED_CONTENT"))

        gradlew(
            testProjectDir,
            ":app:signApkViaServiceRelease",
            "-PsignToken=12345"
        )

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
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("SIGNED_CONTENT"))

        gradlew(
            testProjectDir,
            ":app:signBundleViaServiceRelease",
            "-PsignToken=12345"
        )

        // See explanation for this hack inside SignTask
        val resultArtifact = File(testProjectDir, "app/build/outputs/bundle/release/app-release.aab")
        assertThat(resultArtifact.exists()).isTrue()
        assertThat(resultArtifact.readText()).isEqualTo("SIGNED_CONTENT")
    }

    private fun generateTestProject(signServiceExtension: String = defaultExtension) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    "app",
                    enableKotlinAndroidPlugin = false,
                    versionCode = "100",
                    versionName = "22.1",
                    plugins = plugins {
                        id("com.avito.android.signer")
                    },
                    buildGradleExtra = signServiceExtension
                )
            )
        ).generateIn(testProjectDir)
    }
}
