package com.avito.android.plugin.build_metrics.cache

import com.avito.android.plugin.build_metrics.BuildMetricsRunner
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal abstract class HttpBuildCacheTestFixture {

    private lateinit var projectDir: File
    private lateinit var mockWebServer: MockWebServer

    @BeforeEach
    fun setup(@TempDir tempDir: File) {
        this.projectDir = tempDir
    }

    protected fun setup() {
        mockWebServer = MockWebServer()

        File(projectDir, "settings.gradle.kts").writeText(
            buildCacheBlock(mockWebServer.url("/").toString())
        )
        givenHttpBuildCache()
        setupProject(projectDir)
    }

    abstract fun setupProject(projectDir: File)

    private fun buildCacheBlock(url: String): String {
        return """
            buildCache {
                local {
                    isEnabled = false
                }
                remote<HttpBuildCache> {
                    setUrl("$url")
                    isEnabled = true
                    isPush = true
                    isAllowUntrustedServer = true
                    isAllowInsecureProtocol = true
                }
            }
            """.trimIndent()
    }

    @AfterEach
    fun cleanup() {
        mockWebServer.shutdown()
    }

    // Overriding content is not supported yet, only statuses.
    // Load should return task specific outputs as zip entry in an internal format (see BuildCacheCommandFactory).
    // See content of a cache entry for details.
    protected fun givenHttpBuildCache(
        loadHttpStatus: Int = 404,
        storeHttpStatus: Int = 200
    ) {
        mockWebServer.dispatcher = object : Dispatcher() {

            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.method) {
                    "GET" -> MockResponse().setResponseCode(loadHttpStatus)
                    "PUT" -> MockResponse().setResponseCode(storeHttpStatus)
                    else -> throw IllegalStateException("Unmocked method: ${request.method}")
                }
            }
        }
    }

    protected fun build(vararg args: String) =
        BuildMetricsRunner(projectDir)
            .build(
                args.toList().plus(listOf(
                    "--build-cache",
                    // Cache errors can happen for Kotlin DSL scripts.
                    // It disables cache before applying the plugin for metrics
                    // This is why we have to intercept all errors in tests
                    "-Dorg.gradle.unsafe.build-cache.remote-continue-on-error=true"
                ))
            )
}
