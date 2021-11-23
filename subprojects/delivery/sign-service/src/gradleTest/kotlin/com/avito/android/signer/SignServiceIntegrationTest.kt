package com.avito.android.signer

import com.avito.http.HttpCodes
import com.avito.test.gradle.gradlew
import com.avito.test.http.MockWebServerFactory
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class SignServiceIntegrationTest {

    private val mockWebServer = MockWebServerFactory.create()

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `apk signing task - produces expected file`(@TempDir testProjectDir: File) {
        generateTestProject(
            testProjectDir = testProjectDir,
            buildGradleKtsExtra = """
                |signer {
                |   serviceUrl.set("${mockWebServer.url("/")}")
                |   apkSignTokens.put("$applicationId", "12345")
                |}
                |""".trimMargin()
        )

        mockWebServer.enqueue(MockResponse().setResponseCode(HttpCodes.OK).setBody("SIGNED_CONTENT"))

        gradlew(
            testProjectDir,
            ":$moduleName:signApkViaServiceRelease",
        ).assertThat().buildSuccessful()

        // See explanation for this hack inside SignTask
        val resultArtifact = File(testProjectDir, "app/build/outputs/signService/apk/release/app-release.apk")
        assertThat(resultArtifact.exists()).isTrue()
        assertThat(resultArtifact.readText()).isEqualTo("SIGNED_CONTENT")
    }

    @Test
    fun `bundle signing task - produces expected file`(@TempDir testProjectDir: File) {
        generateTestProject(
            testProjectDir = testProjectDir,
            buildGradleKtsExtra = """
                |signer {
                |   serviceUrl.set("${mockWebServer.url("/")}")
                |   bundleSignTokens.put("$applicationId", "12345")
                |}
                |""".trimMargin()
        )

        mockWebServer.enqueue(MockResponse().setResponseCode(HttpCodes.OK).setBody("SIGNED_CONTENT"))

        gradlew(
            testProjectDir,
            ":$moduleName:signBundleViaServiceRelease",
        ).assertThat().buildSuccessful()

        // See explanation for this hack inside SignTask
        val resultArtifact = File(testProjectDir, "app/build/outputs/signService/bundle/release/app-release.aab")
        assertThat(resultArtifact.exists()).isTrue()
        assertThat(resultArtifact.readText()).isEqualTo("SIGNED_CONTENT")
    }
}
