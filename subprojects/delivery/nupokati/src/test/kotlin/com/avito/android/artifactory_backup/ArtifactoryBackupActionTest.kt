package com.avito.android.artifactory_backup

import com.avito.android.http.ArtifactoryClient
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.utils.ResourcesReader
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

internal class ArtifactoryBackupActionTest {

    private val logger: Logger = Logging.getLogger("ArtifactoryBackupActionTest")

    private val mockDispatcher = MockDispatcher()
    private val mockWebServer = MockWebServer().apply { dispatcher = mockDispatcher }

    @AfterEach
    fun cleanup() {
        mockWebServer.shutdown()
    }

    @Test
    fun test() {
        val url = mockWebServer.url("/")
        val httpClient = OkHttpClient.Builder().build()
        val artifactoryClient = ArtifactoryClient(httpClient)
        val artifactsAdapter = CdBuildResultArtifactsAdapter()
        val artifactoryBackupAction = ArtifactoryBackupAction(artifactoryClient, artifactsAdapter, logger)

        val bundleFileName = "stub.aab"
        val jsonFileName = "stub.json"

        setOf(bundleFileName, jsonFileName).forEach {
            mockDispatcher.registerMock(
                Mock(
                    requestMatcher = {
                        method == "PUT"
                            && path == "/artifactory/mobile-releases/avito_test_android/1792.0_1/$it"
                    },
                    response = MockResponse().setResponseCode(200)
                )
            )
        }

        val json = artifactoryBackupAction.backup(
            artifactoryUploadPath = "${url}artifactory/mobile-releases/avito_test_android/1792.0_1",
            buildVariant = "release",
            files = setOf(ResourcesReader.readFile(bundleFileName), ResourcesReader.readFile(jsonFileName))
        )

        @Language("JSON")
        val expectedJson =
            """{
  "artifacts": [
    {
      "artifact": "binary",
      "type": "bundle",
      "name": "$bundleFileName",
      "uri": "${url}artifactory/mobile-releases/avito_test_android/1792.0_1/$bundleFileName",
      "buildVariant": "release"
    },
    {
      "artifact": "file",
      "type": "json",
      "name": "stub.json",
      "uri": "${url}artifactory/mobile-releases/avito_test_android/1792.0_1/$jsonFileName"
    }
  ]
}"""

        JSONAssert.assertEquals(expectedJson, json, true)
    }
}
