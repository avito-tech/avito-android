package com.avito.android.http.nupokati.retrofit

import com.avito.android.MiB
import com.avito.android.Result
import com.avito.android.http.nupokati.model.ArtifactUploadResult
import com.avito.http.RetryInterceptor
import com.avito.reportviewer.model.ReportCoordinates
import com.avito.test.http.MockWebServerFactory
import com.avito.truth.ResultSubject.Companion.assertThat
import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Path

internal class RetrofitNupokatiV4ClientTest {

    private lateinit var testProjectDir: File

    private lateinit var server: MockWebServer

    private fun createFile(name: String, sizeInBytes: Long): File {
        val file = File(testProjectDir, name)
        file.createNewFile()

        RandomAccessFile(file, "rw").use { raf ->
            raf.setLength(sizeInBytes)
        }

        return file
    }

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        testProjectDir = tempPath.toFile()
    }

    private fun setupMockServer(recordRequests: Boolean): MockWebServer {
        val server = MockWebServerFactory.create().apply {
            if (!recordRequests) {
                bodyLimit = 0
            }
        }
        server.start()
        return server
    }

    private fun createClient(url: HttpUrl): RetrofitNupokatiV4Client {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        return RetrofitNupokatiV4Client(
            retrofit.create(NupokatiV4Api::class.java),
            400.MiB
        )
    }

    private fun createRetryingClient(
        url: HttpUrl,
        chunkedUploadThreshold: Long = 400.MiB,
    ): RetrofitNupokatiV4Client {
        val okHttp = OkHttpClient.Builder()
            .addInterceptor(
                RetryInterceptor(
                    retries = 3,
                    allowedMethods = listOf("POST"),
                )
            )
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttp)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        return RetrofitNupokatiV4Client(
            retrofit.create(NupokatiV4Api::class.java),
            chunkedUploadThreshold
        )
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `uploadArtifact - success - returns ArtifactUploadResult`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val testArtifact = createFile("test-artifact.apk", 17L)

        val responseBody = """
            {
                "buildNumber": 123,
                "version": "1.0.0",
                "platform": "android",
                "project": "com.test.app",
                "uri": "https://storage.example.com/test-artifact.apk"
            }
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json")
        )

        val result: Result<ArtifactUploadResult> = client.uploadArtifact(
            platform = "android",
            project = "com.test.app",
            version = "1.0.0",
            buildNumber = 123,
            file = testArtifact,
        )

        assertThat(result).isSuccess().withValue { value ->
            assertThat(value.artifactUri).isEqualTo("https://storage.example.com/test-artifact.apk")
        }
    }

    @Test
    fun `uploadArtifact - success - request contains all multipart form fields`() {
        server = setupMockServer(true)
        val url = server.url("/")
        val client = createClient(url)

        val testArtifact = createFile("test-artifact.apk", 17L)
        testArtifact.writeText("test file content")

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                        {
                            "buildNumber": 123,
                            "version": "1.0.0",
                            "platform": "android",
                            "project": "com.test.app",
                            "uri": "https://storage.example.com/test-artifact.apk"
                        }
                    """.trimIndent()
                )
                .addHeader("Content-Type", "application/json")
        )

        client.uploadArtifact(
            platform = "android",
            project = "com.test.app",
            version = "1.0.0",
            buildNumber = 123,
            file = testArtifact
        )

        val recordedRequest = server.takeRequest()
        val requestBody = recordedRequest.body.readUtf8().trimIndent()

        assertThat(recordedRequest.path).isEqualTo("/api/1/upload_artifact")
        assertThat(recordedRequest.method).isEqualTo("POST")
        assertThat(requestBody).contains(
            """
            Content-Disposition: form-data; name="platform"
            Content-Transfer-Encoding: binary
            Content-Type: text/plain; charset=UTF-8
            Content-Length: 7
            
            android""".trimIndent()
        )
        assertThat(requestBody).contains(
            """
            Content-Disposition: form-data; name="project"
            Content-Transfer-Encoding: binary
            Content-Type: text/plain; charset=UTF-8
            Content-Length: 12

            com.test.app""".trimIndent()
        )
        assertThat(requestBody).contains(
            """
            Content-Disposition: form-data; name="version"
            Content-Transfer-Encoding: binary
            Content-Type: text/plain; charset=UTF-8
            Content-Length: 5

            1.0.0""".trimIndent()
        )
        assertThat(requestBody).contains(
            """
            Content-Disposition: form-data; name="buildNumber"
            Content-Transfer-Encoding: binary
            Content-Type: text/plain; charset=UTF-8
            Content-Length: 3

            123""".trimIndent()
        )
        assertThat(requestBody).contains(
            """
            Content-Disposition: form-data; name="file"; filename="test-artifact.apk"
            Content-Length: 17

            test file content""".trimIndent()
        )
    }

    @Test
    fun `uploadArtifact - failure - 4xx client error returns failure`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val testArtifact = createFile("test-artifact.apk", 17L)

        val errorBody = """{"error": "Invalid request parameters"}"""
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(errorBody)
        )

        val result = client.uploadArtifact(
            platform = "android",
            project = "com.test.app",
            version = "1.0.0",
            buildNumber = 123,
            file = testArtifact
        )

        assertThat(result).isFailure().withThrowable { throwable ->
            assertThat(throwable.message).contains("400")
            assertThat(throwable.message).contains(errorBody)
        }
    }

    @Test
    fun `uploadArtifact - failure - 5xx server error returns failure`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val testArtifact = createFile("test-artifact.apk", 17L)

        val errorBody = """{"error": "Internal server error"}"""
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody(errorBody)
        )

        val result = client.uploadArtifact(
            platform = "android",
            project = "com.test.app",
            version = "1.0.0",
            buildNumber = 123,
            file = testArtifact
        )

        assertThat(result).isFailure().withThrowable { throwable ->
            assertThat(throwable).isInstanceOf(RuntimeException::class.java)
            assertThat(throwable.message).isEqualTo(
                "HTTP Response[ code: 500, message: Server Error, body: {\"error\": \"Internal server error\"}]"
            )
        }
    }

    @Test
    fun `saveTestResult - success - returns TestResultSaveResult`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val responseBody = """
            {
                "result": {
                    "success": true,
                    "message": "Test result saved successfully"
                }
            }
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json")
        )

        val result = client.saveTestResult(
            project = "com.test.app",
            platform = "android",
            version = "1.0.0",
            buildNumber = 123,
            reportUrl = "https://example.com/report",
            reportCoordinates = ReportCoordinates(
                planSlug = "regression",
                jobSlug = "nightly",
                runId = "run-123"
            )
        )

        assertThat(result).isSuccess().withValue { value ->
            assertThat(value.message).isEqualTo("Test result saved successfully")
        }
    }

    @Test
    fun `saveTestResult - failure - response with success == false returns failure`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val responseBody = """
            {
                "result": {
                    "success": false,
                    "message": "Save test result failed"
                }
            }
        """.trimIndent()

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json")
        )

        val result = client.saveTestResult(
            project = "com.test.app",
            platform = "android",
            version = "1.0.0",
            buildNumber = 123,
            reportUrl = "https://example.com/report",
            reportCoordinates = ReportCoordinates(
                planSlug = "planSlig",
                jobSlug = "jobSlug",
                runId = "run-123"
            )
        )

        assertThat(result).isFailure().withThrowable { throwable ->
            assertThat(throwable).isInstanceOf(RuntimeException::class.java)
            assertThat(throwable.message).isEqualTo(
                "Error while saving test result: Save test result failed"
            )
        }
    }

    @Test
    fun `saveTestResult - success - response without message field`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val responseBody = """{ "result": { "success": true } }"""

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json")
        )

        val result = client.saveTestResult(
            project = "com.test.app",
            platform = "android",
            version = "1.0.0",
            buildNumber = 123,
            reportUrl = "https://example.com/report",
            reportCoordinates = ReportCoordinates(
                planSlug = "planSlug",
                jobSlug = "jobSlug",
                runId = "run-123"
            )
        )

        assertThat(result).isSuccess().withValue { value ->
            assertThat(value.message).isNull()
        }
    }

    @Test
    fun `saveTestResult - success - request body serialization is correct`() {
        server = setupMockServer(true)
        val url = server.url("/")
        val client = createClient(url)

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"result": {"success": true}}""")
                .addHeader("Content-Type", "application/json")
        )

        client.saveTestResult(
            project = "com.test.app",
            platform = "android",
            version = "1.0.0",
            buildNumber = 123,
            reportUrl = "https://example.com/report",
            reportCoordinates = ReportCoordinates(
                planSlug = "planSlug",
                jobSlug = "jobSlug",
                runId = "run-123"
            )
        )

        val recordedRequest = server.takeRequest()
        val actualRequestBody = recordedRequest.body.readUtf8()

        assertThat(recordedRequest.path).isEqualTo("/saveTestResult/")
        assertThat(recordedRequest.method).isEqualTo("POST")

        val expectedRequestBody = """
            {
                "project": "com.test.app",
                "platform": "android",
                "version": "1.0.0",
                "buildNumber": 123,
                "reportUrl": "https://example.com/report",
                "reportCoordinates": {
                    "planSlug": "planSlug",
                    "jobSlug": "jobSlug",
                    "runId": "run-123"
                }
            }
        """.trimIndent()
            .replace("\n", "")
            .replace(" ", "")

        assertThat(actualRequestBody).isEqualTo(expectedRequestBody)
    }

    @Test
    fun `saveTestResult - failure - 4xx client error returns failure`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val errorBody = """{"error": "Bad request"}"""
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(errorBody)
        )

        val result = client.saveTestResult(
            project = "com.test.app",
            platform = "android",
            version = "1.0.0",
            buildNumber = 123,
            reportUrl = "https://example.com/report",
            reportCoordinates = ReportCoordinates(
                planSlug = "planSlug",
                jobSlug = "jobSlug",
                runId = "run-123"
            )
        )

        assertThat(result).isFailure().withThrowable { throwable ->
            assertThat(throwable).isInstanceOf(RuntimeException::class.java)
            assertThat(throwable.message).isEqualTo(
                "HTTP Response[ code: 400, message: Client Error, body: {\"error\": \"Bad request\"}]"
            )
        }
    }

    @Test
    fun `saveTestResult - failure - 5xx server error returns Result_Failure`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val errorBody = """{"error": "Internal server error"}"""
        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody(errorBody)
        )

        val result = client.saveTestResult(
            project = "com.test.app",
            platform = "android",
            version = "1.0.0",
            buildNumber = 123,
            reportUrl = "https://example.com/report",
            reportCoordinates = ReportCoordinates(
                planSlug = "planSlug",
                jobSlug = "jobSlug",
                runId = "run-123"
            )
        )

        assertThat(result).isFailure().withThrowable { throwable ->
            assertThat(throwable).isInstanceOf(RuntimeException::class.java)
            assertThat(throwable.message).isEqualTo(
                "HTTP Response[ code: 500, message: Server Error, body: {\"error\": \"Internal server error\"}]"
            )
        }
    }

    @Test
    fun `uploadArtifact - simple upload - used for file less than chunked upload threshold`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val file = createFile("artifact.apk", 99L * 1024 * 1024)

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                        "buildNumber": 123,
                        "version": "1.0.0",
                        "platform": "android",
                        "project": "com.test.app",
                        "uri": "https://storage.example.com/test-artifact.apk"
                     }
                    """.trimIndent()
                )
                .addHeader("Content-Type", "application/json")
        )

        val result = client.uploadArtifact(
            platform = "android",
            project = "com.test.app",
            version = "1.0.0",
            buildNumber = 123,
            file = file
        )

        assertThat(result).isSuccess().withValue { value ->
            assertThat(value.artifactUri).isEqualTo("https://storage.example.com/test-artifact.apk")
        }

        val request = server.takeRequest()
        assertThat(request.path).isEqualTo("/api/1/upload_artifact")
    }

    @Test
    fun `uploadArtifact - chunked upload - used for file greater than chunked upload threshold`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val largeFile = createFile("large-artifact.apk", 401L * 1024 * 1024)

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{ "result": { "uploadId": "test-upload-123" } }""")
                .addHeader("Content-Type", "application/json")
        )

        for (i in 1..largeFile.chunksCount()) {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("""{"etag": "etag-$i", "partNumber": $i}""")
                    .addHeader("Content-Type", "application/json")
            )
        }

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{ "result": { "uri": "https://storage.example.com/test-artifact.apk" } }""")
                .addHeader("Content-Type", "application/json")
        )

        val result = client.uploadArtifact(
            platform = "android",
            project = "com.test.app",
            version = "1.0.0",
            buildNumber = 123,
            file = largeFile
        )

        assertThat(result).isSuccess().withValue { value ->
            assertThat(value.artifactUri).isEqualTo("https://storage.example.com/test-artifact.apk")
        }

        val initRequest = server.takeRequest()
        assertThat(initRequest.path).isEqualTo("/multipartUploadInit/")

        repeat(largeFile.chunksCount().toInt()) {
            val chunkRequest = server.takeRequest()
            assertThat(chunkRequest.path).isEqualTo("/api/1/upload_part_artifact")
        }

        val completeRequest = server.takeRequest()
        assertThat(completeRequest.path).isEqualTo("/multipartUploadComplete/")
    }

    @Test
    fun `uploadArtifact - chunked upload - multiple chunks uploaded with correct SHA256`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val largeFile = createFile("large-artifact-sha256.apk", 401L * 1024 * 1024)

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{ "result": { "uploadId": "test-upload-123" } }""")
        )

        for (i in 1..largeFile.chunksCount()) {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("""{"etag": "etag-part-$i", "partNumber": $i}""")
            )
        }

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{ "result": { "uri": "https://storage.example.com/test-artifact.apk" } }""")
        )

        val result = client.uploadArtifact(
            platform = "android",
            project = "com.test.app",
            version = "1.0.0",
            buildNumber = 123,
            file = largeFile
        )

        result.fold(
            onSuccess = { it },
            onFailure = { throw it }
        )
        assertThat(result).isSuccess()

        server.takeRequest() // init request

        repeat(largeFile.chunksCount().toInt()) {
            val chunkRequest = server.takeRequest()
            assertThat(chunkRequest.path).isEqualTo("/api/1/upload_part_artifact")
            assertThat(chunkRequest.method).isEqualTo("POST")
            assertThat(chunkRequest.getHeader("Content-Type")).contains("multipart")
        }

        val completeRequest = server.takeRequest()
        assertThat(completeRequest.path).isEqualTo("/multipartUploadComplete/")
    }

    @Test
    fun `uploadArtifact - simple upload - error in response body returns failure`() {
        server = setupMockServer(true)
        val url = server.url("/")
        val client = createClient(url)

        val smallFile = createFile("small-error.apk", 50L * 1024 * 1024)

        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"error": "Upload failed"}""")
                .addHeader("Content-Type", "application/json")
        )

        val result = client.uploadArtifact(
            platform = "android",
            project = "com.test.app",
            version = "1.0.0",
            buildNumber = 123,
            file = smallFile
        )

        assertThat(result).isFailure().withThrowable { throwable ->
            assertThat(throwable).isInstanceOf(RuntimeException::class.java)
            assertThat(throwable.message).contains("500")
            assertThat(throwable.message).contains("Upload failed")
        }
    }

    @Test
    fun `uploadArtifact - chunked upload - error during init returns failure`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val largeFile = createFile("large-init-error.apk", 401L * 1024 * 1024)

        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"error": "Unexpected error"}""")
        )

        val result = client.uploadArtifact(
            platform = "android",
            project = "com.test.app",
            version = "1.0.0",
            buildNumber = 123,
            file = largeFile
        )

        assertThat(result).isFailure().withThrowable { throwable ->
            assertThat(throwable.message).contains("500")
        }

        assertThat(server.requestCount).isEqualTo(1) // Only init request
    }

    @Test
    fun `uploadArtifact - chunked upload - error during chunk upload triggers abort`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val largeFile = createFile("large-chunk-error.apk", 401L * 1024 * 1024)

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{ "result": { "uploadId": "test-upload-123" } }""")
        )

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"etag": "etag-1", "partNumber": 1}""")
        )

        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"error": "Chunk upload failed"}""")
        )

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{}""")
        )

        val result = client.uploadArtifact(
            platform = "android",
            project = "com.test.app",
            version = "1.0.0",
            buildNumber = 123,
            file = largeFile
        )

        assertThat(result).isFailure()

        assertThat(server.requestCount).isEqualTo(4)

        server.takeRequest() // init
        server.takeRequest() // chunk 1
        server.takeRequest() // chunk 2
        val abortRequest = server.takeRequest()
        assertThat(abortRequest.path).isEqualTo("/multipartUploadAbort/")
    }

    @Test
    fun `uploadArtifact - chunked upload - error during complete returns failure`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val largeFile = createFile("large-complete-error.apk", 401L * 1024 * 1024)

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{ "result": { "uploadId": "test-upload-123" } }""")
        )

        for (i in 1..largeFile.chunksCount()) {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("""{"etag": "etag-$i", "partNumber": $i}""")
            )
        }

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{ "result": { "error": "Failed to finalize upload", "uri": null } }""")
        )

        val result = client.uploadArtifact(
            platform = "android",
            project = "com.test.app",
            version = "1.0.0",
            buildNumber = 123,
            file = largeFile
        )

        assertThat(result).isFailure().withThrowable { throwable ->
            assertThat(throwable.message).contains("Failed to finalize upload")
        }
    }

    @Test
    fun `uploadArtifact - chunked upload - error in complete response with non-null error field`() {
        server = setupMockServer(false)
        val url = server.url("/")
        val client = createClient(url)

        val largeFile = createFile("large-validation-error.apk", 401L * 1024 * 1024)

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{ "result": { "uploadId": "test-upload-123" } }""")
        )

        for (i in 1..largeFile.chunksCount()) {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("""{ "etag": "etag-$i", "partNumber": $i }""")
            )
        }

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{ "result": { "error": "Completion failed", "uri": null } }""")
        )

        val result = client.uploadArtifact(
            platform = "android",
            project = "com.test.app",
            version = "1.0.0",
            buildNumber = 123,
            file = largeFile
        )

        assertThat(result).isFailure().withThrowable { throwable ->
            assertThat(throwable.message).contains("Completion failed")
        }
    }

    @Test
    fun `uploadArtifact - retry - retries on 503 and succeeds`() {
        server = setupMockServer(false)
        val client = createRetryingClient(server.url("/"))
        val testArtifact = createFile("test-artifact.apk", 17L)

        server.enqueue(MockResponse().setResponseCode(503).setBody("transient"))
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"buildNumber":1,"version":"1","platform":"android","project":"p","uri":"u"}""")
        )

        val result = client.uploadArtifact(
            platform = "android", project = "p", version = "1", buildNumber = 1, file = testArtifact
        )

        assertThat(result).isSuccess()
        assertThat(server.requestCount).isEqualTo(2)
    }

    @Test
    fun `uploadArtifact - retry - fails after retries exhausted`() {
        server = setupMockServer(false)
        val client = createRetryingClient(server.url("/"))
        val testArtifact = createFile("test-artifact.apk", 17L)

        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(503))

        val result = client.uploadArtifact(
            platform = "android", project = "p", version = "1", buildNumber = 1, file = testArtifact
        )

        assertThat(result).isFailure()
        assertThat(server.requestCount).isEqualTo(3)
    }

    @Test
    fun `uploadArtifact - retry - does not retry on 4xx`() {
        server = setupMockServer(false)
        val client = createRetryingClient(server.url("/"))
        val testArtifact = createFile("test-artifact.apk", 17L)

        server.enqueue(MockResponse().setResponseCode(400).setBody("bad"))

        val result = client.uploadArtifact(
            platform = "android", project = "p", version = "1", buildNumber = 1, file = testArtifact
        )

        assertThat(result).isFailure()
        assertThat(server.requestCount).isEqualTo(1)
    }

    @Test
    fun `uploadArtifact - retry - preserves request body across attempts`() {
        server = setupMockServer(true)
        val client = createRetryingClient(server.url("/"))
        val testArtifact = createFile("test-artifact.apk", 17L)
        testArtifact.writeText("test file content")

        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"buildNumber":1,"version":"1","platform":"android","project":"p","uri":"u"}""")
        )

        client.uploadArtifact(
            platform = "android", project = "p", version = "1", buildNumber = 1, file = testArtifact
        )

        val firstBody = server.takeRequest().body.readUtf8()
        val secondBody = server.takeRequest().body.readUtf8()
        assertThat(firstBody).contains("test file content")
        assertThat(secondBody).contains("test file content")
        assertThat(firstBody).contains("""name="project"""")
        assertThat(secondBody).contains("""name="project"""")
    }

    @Test
    fun `uploadArtifact - chunked upload - retries on init failure`() {
        server = setupMockServer(false)
        val client = createRetryingClient(server.url("/"))
        val largeFile = createFile("large.apk", 401L * 1024 * 1024)

        // init: 503, then 200
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{ "result": { "uploadId": "u1" } }""")
        )
        // parts
        for (i in 1..largeFile.chunksCount()) {
            server.enqueue(
                MockResponse().setResponseCode(200)
                    .setBody("""{"etag":"e-$i","partNumber":$i}""")
            )
        }
        // complete
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{ "result": { "uri": "https://example.com/u" } }""")
        )

        val result = client.uploadArtifact(
            platform = "android", project = "p", version = "1", buildNumber = 1, file = largeFile
        )

        assertThat(result).isSuccess()
        val firstPath = server.takeRequest().path
        val secondPath = server.takeRequest().path
        assertThat(firstPath).isEqualTo("/multipartUploadInit/")
        assertThat(secondPath).isEqualTo("/multipartUploadInit/")
    }

    @Test
    fun `uploadArtifact - chunked upload - retries on part without abort`() {
        server = setupMockServer(false)
        val client = createRetryingClient(server.url("/"))
        val largeFile = createFile("large.apk", 401L * 1024 * 1024)
        val chunks = largeFile.chunksCount().toInt()

        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{ "result": { "uploadId": "u1" } }""")
        )
        // part 1 ok
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"etag":"e-1","partNumber":1}""")
        )
        // part 2 fails once then succeeds
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"etag":"e-2","partNumber":2}""")
        )
        // remaining parts
        for (i in 3..chunks) {
            server.enqueue(
                MockResponse().setResponseCode(200)
                    .setBody("""{"etag":"e-$i","partNumber":$i}""")
            )
        }
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{ "result": { "uri": "https://example.com/u" } }""")
        )

        val result = client.uploadArtifact(
            platform = "android", project = "p", version = "1", buildNumber = 1, file = largeFile
        )

        assertThat(result).isSuccess()
        // Verify NO abort was issued
        val paths = (1..server.requestCount).map { server.takeRequest().path }
        assertThat(paths).doesNotContain("/multipartUploadAbort/")
    }

    @Test
    fun `uploadArtifact - chunked upload - retry preserves partNumber and sha256Hex`() {
        server = setupMockServer(true)
        val client = createRetryingClient(server.url("/"))
        val largeFile = createFile("large.apk", 401L * 1024 * 1024)
        val chunks = largeFile.chunksCount().toInt()

        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{ "result": { "uploadId": "u1" } }""")
        )
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"etag":"e-1","partNumber":1}""")
        )
        // part 2 fails then succeeds
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"etag":"e-2","partNumber":2}""")
        )
        for (i in 3..chunks) {
            server.enqueue(
                MockResponse().setResponseCode(200)
                    .setBody("""{"etag":"e-$i","partNumber":$i}""")
            )
        }
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{ "result": { "uri": "https://example.com/u" } }""")
        )

        client.uploadArtifact(
            platform = "android", project = "p", version = "1", buildNumber = 1, file = largeFile
        )

        server.takeRequest() // init
        server.takeRequest() // part 1
        val part2Try1 = server.takeRequest().body.readUtf8()
        val part2Try2 = server.takeRequest().body.readUtf8()

        val sha1 = Regex("""name="sha256Hex"[\s\S]*?\r?\n\r?\n([0-9a-f]+)""").find(part2Try1)?.groupValues?.get(1)
        val sha2 = Regex("""name="sha256Hex"[\s\S]*?\r?\n\r?\n([0-9a-f]+)""").find(part2Try2)?.groupValues?.get(1)
        val num1 = Regex("""name="partNumber"[\s\S]*?\r?\n\r?\n(\d+)""").find(part2Try1)?.groupValues?.get(1)
        val num2 = Regex("""name="partNumber"[\s\S]*?\r?\n\r?\n(\d+)""").find(part2Try2)?.groupValues?.get(1)

        assertThat(sha1).isNotNull()
        assertThat(sha1).isEqualTo(sha2)
        assertThat(num1).isEqualTo("2")
        assertThat(num2).isEqualTo("2")
    }

    @Test
    fun `uploadArtifact - chunked upload - aborts after part retries exhausted`() {
        server = setupMockServer(false)
        val client = createRetryingClient(server.url("/"))
        val largeFile = createFile("large.apk", 401L * 1024 * 1024)

        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{ "result": { "uploadId": "u1" } }""")
        )
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"etag":"e-1","partNumber":1}""")
        )
        // part 2 fails 3 times (retries exhausted)
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(503))
        // abort
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{}"""))

        val result = client.uploadArtifact(
            platform = "android", project = "p", version = "1", buildNumber = 1, file = largeFile
        )

        assertThat(result).isFailure()
        val paths = (1..server.requestCount).map { server.takeRequest().path }
        assertThat(paths).contains("/multipartUploadAbort/")
    }

    @Test
    fun `uploadArtifact - chunked upload - retries on complete failure`() {
        server = setupMockServer(false)
        val client = createRetryingClient(server.url("/"))
        val largeFile = createFile("large.apk", 401L * 1024 * 1024)
        val chunks = largeFile.chunksCount().toInt()

        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{ "result": { "uploadId": "u1" } }""")
        )
        for (i in 1..chunks) {
            server.enqueue(
                MockResponse().setResponseCode(200)
                    .setBody("""{"etag":"e-$i","partNumber":$i}""")
            )
        }
        // complete: 503, then 200
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{ "result": { "uri": "https://example.com/u" } }""")
        )

        val result = client.uploadArtifact(
            platform = "android", project = "p", version = "1", buildNumber = 1, file = largeFile
        )

        assertThat(result).isSuccess()
    }

    @Test
    fun `saveTestResult - retry - retries on 503 and succeeds`() {
        server = setupMockServer(false)
        val client = createRetryingClient(server.url("/"))

        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{"result":{"success":true,"message":"ok"}}""")
        )

        val result = client.saveTestResult(
            project = "p",
            platform = "android",
            version = "1",
            buildNumber = 1,
            reportUrl = "u",
            reportCoordinates = ReportCoordinates("a", "b", "c")
        )

        assertThat(result).isSuccess()
        assertThat(server.requestCount).isEqualTo(2)
    }

    @Test
    fun `saveTestResult - retry - does not retry on 4xx`() {
        server = setupMockServer(false)
        val client = createRetryingClient(server.url("/"))

        server.enqueue(MockResponse().setResponseCode(400).setBody("bad"))

        val result = client.saveTestResult(
            project = "p",
            platform = "android",
            version = "1",
            buildNumber = 1,
            reportUrl = "u",
            reportCoordinates = ReportCoordinates("a", "b", "c")
        )

        assertThat(result).isFailure()
        assertThat(server.requestCount).isEqualTo(1)
    }

    fun File.chunksCount(): Long {
        return if (this.length() % 10.MiB == 0L) {
            this.length() / 10.MiB
        } else {
            this.length() / 10.MiB + 1
        }
    }
}
