package com.avito.android.http.nupokati.retrofit

import com.avito.android.MiB
import com.avito.android.stats.StubStatsdSender
import com.avito.http.RetryInterceptor
import com.avito.http.StatsDHttpEventListener
import com.avito.http.TagRequestMetadataProvider
import com.avito.logger.PrintlnLoggerFactory
import com.avito.test.http.MockWebServerFactory
import com.avito.time.DefaultTimeProvider
import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
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
import java.util.concurrent.TimeUnit

internal class NupokatiMetricsTest {

    private val statsDSender = StubStatsdSender()
    private lateinit var server: MockWebServer
    private lateinit var testProjectDir: File

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        testProjectDir = tempPath.toFile()
        server = MockWebServerFactory.create().apply { bodyLimit = 0 }
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    private fun createFile(name: String, sizeInBytes: Long): File {
        val file = File(testProjectDir, name)
        file.createNewFile()
        RandomAccessFile(file, "rw").use { raf -> raf.setLength(sizeInBytes) }
        return file
    }

    private fun createClient(
        url: HttpUrl,
        withRetry: Boolean = true,
        readTimeoutMs: Long? = null,
    ): RetrofitNupokatiV4Client {
        val builder = OkHttpClient.Builder()
        if (withRetry) {
            builder.addInterceptor(
                RetryInterceptor(
                    retries = 3,
                    allowedMethods = listOf("POST"),
                )
            )
        }
        if (readTimeoutMs != null) {
            builder.readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
        }
        builder.eventListenerFactory {
            StatsDHttpEventListener(
                statsDSender = statsDSender,
                timeProvider = DefaultTimeProvider(),
                requestMetadataProvider = TagRequestMetadataProvider(),
                loggerFactory = PrintlnLoggerFactory,
            )
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(builder.build())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        return RetrofitNupokatiV4Client(
            retrofit.create(NupokatiV4Api::class.java),
            400.MiB
        )
    }

    @Test
    fun `metric - success - emits endpoint with 200 code`() {
        val client = createClient(server.url("/"))
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"buildNumber":1,"version":"1","platform":"android","project":"p","uri":"u"}"""
            )
        )

        client.uploadArtifact(
            platform = "android", project = "p", version = "1", buildNumber = 1,
            file = createFile("small.apk", 17L)
        )

        val names = statsDSender.getSentMetrics().map { it.name.toString() }
        assertThat(names).contains("network.nupokati.upload_artifact.200")
    }

    @Test
    fun `metric - failure - emits endpoint with 5xx code`() {
        // Disable retry to isolate metric for the single 503 response
        val client = createClient(server.url("/"), withRetry = false)
        server.enqueue(MockResponse().setResponseCode(503))

        client.uploadArtifact(
            platform = "android", project = "p", version = "1", buildNumber = 1,
            file = createFile("small.apk", 17L)
        )

        val names = statsDSender.getSentMetrics().map { it.name.toString() }
        assertThat(names).contains("network.nupokati.upload_artifact.503")
    }

    @Test
    fun `metric - timeout - emits endpoint with timeout code`() {
        val client = createClient(server.url("/"), withRetry = false, readTimeoutMs = 100)
        server.enqueue(
            MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE)
        )

        client.uploadArtifact(
            platform = "android", project = "p", version = "1", buildNumber = 1,
            file = createFile("small.apk", 17L)
        )

        val names = statsDSender.getSentMetrics().map { it.name.toString() }
        assertThat(names).contains("network.nupokati.upload_artifact.timeout")
    }

    @Test
    fun `metric - retry - emits one metric per physical attempt`() {
        val client = createClient(server.url("/"), withRetry = true)
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"buildNumber":1,"version":"1","platform":"android","project":"p","uri":"u"}"""
            )
        )

        client.uploadArtifact(
            platform = "android", project = "p", version = "1", buildNumber = 1,
            file = createFile("small.apk", 17L)
        )

        val names = statsDSender.getSentMetrics().map { it.name.toString() }
        val filtered = names.filter { it.startsWith("network.nupokati.upload_artifact.") }
        assertThat(filtered).containsExactly(
            "network.nupokati.upload_artifact.503",
            "network.nupokati.upload_artifact.200"
        ).inOrder()
    }

    @Test
    fun `metric - chunked upload - emits correct endpoint names for init part complete`() {
        val client = createClient(server.url("/"))
        val largeFile = createFile("large.apk", 401L * 1024 * 1024)
        val chunks = if (largeFile.length() % (10L * 1024 * 1024) == 0L) {
            largeFile.length() / (10L * 1024 * 1024)
        } else {
            largeFile.length() / (10L * 1024 * 1024) + 1
        }.toInt()

        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{ "result": { "uploadId": "u1" } }""")
        )
        repeat(chunks) { i ->
            server.enqueue(
                MockResponse().setResponseCode(200)
                    .setBody("""{"etag":"e-${i + 1}","partNumber":${i + 1}}""")
            )
        }
        server.enqueue(
            MockResponse().setResponseCode(200)
                .setBody("""{ "result": { "uri": "https://example.com/u" } }""")
        )

        client.uploadArtifact(
            platform = "android", project = "p", version = "1", buildNumber = 1, file = largeFile
        )

        val names = statsDSender.getSentMetrics().map { it.name.toString() }
        assertThat(names).contains("network.nupokati.upload_part_artifact.200")
        assertThat(names).contains("network.nupokati.multipartUploadInit.200")
        assertThat(names).contains("network.nupokati.multipartUploadComplete.200")
    }
}
