package com.avito.android.signer

import com.avito.http.HttpCodes
import com.avito.test.gradle.gradlew
import com.avito.test.http.MockWebServerFactory
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.tls.HeldCertificate
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
                |   useTls.set(false)
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
                |   useTls.set(false)
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

    @Test
    fun `apk signing task - with mTLS enabled - produces expected file`(@TempDir testProjectDir: File) {
        val tlsFiles = createTlsFiles(testProjectDir)

        generateTestProject(
            testProjectDir = testProjectDir,
            rootBuildGradleKtsExtra = """
                tls {
                    credentials {
                        registerProvider(
                            "signerTls",
                            com.avito.android.tls.extensions.configuration.FilesTlsCredentialsConfiguration::class.java
                        ) {
                            crtFilePath.set("${tlsFiles.certificate.absolutePath}")
                            keyFilePath.set("${tlsFiles.privateKey.absolutePath}")
                        }
                    }
                }
            """.trimIndent(),
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

        val resultArtifact = File(testProjectDir, "app/build/outputs/signService/apk/release/app-release.apk")
        assertThat(resultArtifact.exists()).isTrue()
        assertThat(resultArtifact.readText()).isEqualTo("SIGNED_CONTENT")
    }

    private fun createTlsFiles(projectDir: File): TlsFiles {
        val heldCertificate = HeldCertificate.Builder()
            .commonName("signer-client")
            .build()

        val cert = File(projectDir, "signer-client.crt").apply {
            writeText(heldCertificate.certificatePem())
        }
        val key = File(projectDir, "signer-client.key").apply {
            writeText(heldCertificate.privateKeyPkcs8Pem())
        }

        return TlsFiles(
            certificate = cert,
            privateKey = key
        )
    }

    private data class TlsFiles(
        val certificate: File,
        val privateKey: File
    )
}
