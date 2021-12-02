package com.avito.bitbucket

import com.avito.http.HttpClientProvider
import com.avito.http.createStubInstance
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.commit
import com.avito.test.gradle.file
import com.avito.test.gradle.getCommitHash
import com.avito.test.gradle.git
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import com.avito.truth.ResultSubject.Companion.assertThat
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class BitbucketImplTest {

    private val mockWebServer = MockWebServerFactory.create()

    private val dispatcher = MockDispatcher(
        unmockedResponse = MockResponse().setResponseCode(200),
    )
        .also { dispatcher -> mockWebServer.dispatcher = dispatcher }

    @Test
    fun addInsights(@TempDir tempDir: File) {
        TestProjectGenerator(modules = listOf(KotlinModule("app"))).generateIn(tempDir)

        with(tempDir) {
            git("init --quiet")

            val fileToBeModifiedPath = "app/src/main/kotlin/Resources.kt"
            val notModifiedFilePath = "app/src/main/kotlin/Unchanged.kt"

            createSourceFiles(fileToBeModifiedPath, notModifiedFilePath)
            commit("initial")
            val targetCommit = getCommitHash()

            git("branch develop")
            git("branch feature")

            modifyOneSourceFile(fileToBeModifiedPath)
            commit("changes")
            val sourceHash = getCommitHash()

            dispatcher.registerMock(
                Mock(
                    requestMatcher = { path.contains("rest/insights/1.0") && method == "PUT" },
                    response = MockResponse().setBody("""{ "createdDate": 12345 , "result": "PASS"  }""")
                )
            )

            val addAnnotationsRequest =
                dispatcher.captureRequest { path.contains("rest/insights/1.0") && method == "POST" }

            val result = createBitbucket().addInsights(
                rootDir = tempDir,
                sourceCommitHash = sourceHash,
                targetCommitHash = targetCommit,
                key = "SomeKey",
                title = "SomeTitle",
                link = "http://localhost".toHttpUrl(),
                issues = listOf(
                    Bitbucket.InsightIssue(
                        message = "Unsafe use of nullable receiver of type URL?",
                        path = fileToBeModifiedPath,
                        line = 6,
                        severity = Severity.LOW
                    ),
                    Bitbucket.InsightIssue(
                        message = "Something went terribly wrong",
                        path = notModifiedFilePath,
                        line = 1,
                        severity = Severity.HIGH
                    )
                )
            )

            assertThat(result).isSuccess()

            @Suppress("MaxLineLength")
            addAnnotationsRequest.checks.singleRequestCaptured().bodyContains(
                """{"annotations":[{"path":"$fileToBeModifiedPath","line":6,"message":"Unsafe use of nullable receiver of type URL?","severity":"MEDIUM"}]}"""
            )
        }
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    private fun createBitbucket(): Bitbucket = Bitbucket.create(
        bitbucketConfig = BitbucketConfig(
            baseUrl = mockWebServer.url("/").toString(),
            projectKey = "project",
            repositorySlug = "X",
            credentials = AtlassianCredentials("", "")
        ),
        pullRequestId = null,
        httpClientProvider = HttpClientProvider.createStubInstance()
    )

    private fun File.createSourceFiles(fileToBeModifiedPath: String, notModifiedFilePath: String) {
        file(
            fileToBeModifiedPath,
            """package com.avito.test.gradle

import java.io.File
import java.net.URL

inline fun <reified C> fileFromJarResources(name: String) = File(C::class.java.classLoader.getResource(name).file)

inline fun <reified C> resourceFrom(name: String): URL = C::class.java.classLoader.getResource(name)
""".trimIndent()
        )

        file(
            notModifiedFilePath,
            """package com.avito.test.gradle

fun helloWorld() {
}
""".trimIndent()
        )
    }

    private fun File.modifyOneSourceFile(fileToBeModifiedPath: String) {
        file(
            fileToBeModifiedPath,
            """package com.avito.test.gradle

import java.io.File
import java.net.URL

inline fun <reified C> fileFromJarResources(name: String) = File(C::class.java.classLoader.getResource(name).file)

inline fun <reified C> resourceFrom(name: String): URL = C::class.java.classLoader.getResource(name)

inline fun <reified C> newFunction(name: String): URL = C::class.java.classLoader.getResource(name)
""".trimIndent()
        )
    }
}
