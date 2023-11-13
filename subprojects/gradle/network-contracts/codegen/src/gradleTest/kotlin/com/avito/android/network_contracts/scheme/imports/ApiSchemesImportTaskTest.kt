package com.avito.android.network_contracts.scheme.imports

import com.avito.android.network_contracts.NetworkCodegenProjectGenerator
import com.avito.android.network_contracts.scheme.imports.mocks.apiSchemaImportResponseMock
import com.avito.android.network_contracts.scheme.imports.mocks.apiSchemeEmptyResponse
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.TestResultSubject
import com.avito.test.gradle.gradlew
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ApiSchemesImportTaskTest {

    private val mockDispatcher = MockDispatcher(unmockedResponse = apiSchemaImportResponseMock())
    private val mockWebServer = MockWebServerFactory.create()
        .apply {
            dispatcher = mockDispatcher
        }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @BeforeEach
    fun startup() {
        mockWebServer.start()
    }

    @Test
    fun `when service return schema - then create files`(@TempDir projectDir: File) {
        val apiPath = "/test/api/path"
        val files = listOf(
            "error.yaml",
            "v2/schema.yaml",
            "schema.yaml"
        )
        val packageName = "com.example"
        generateProject(
            projectDir = projectDir,
            generatedClassesPackage = packageName
        )

        mockDispatcher.registerMock(Mock(
            requestMatcher = { path.contains("getSchemaForPath") },
            response = apiSchemaImportResponseMock(
                path = apiPath,
                filePaths = files
            )
        ))

        val request = mockDispatcher.captureRequest { path.contains("getSchemaForPath") }

        runTask(projectDir, apiPath)
            .assertThat()
            .buildSuccessful()
            .outputContainsFilePaths(packageName, apiPath, files)

        request.checks.singleRequestCaptured()
            .bodyContains(apiPath)
    }

    @Test
    fun `when api schemes url is not defined - then throw exception`(@TempDir projectDir: File) {
        generateProject(projectDir)
        runTask(projectDir, apiPath = "", failure = true)
            .assertThat()
            .buildFailed()
            .outputContains("apiSchemesUrl")
    }

    @Test
    fun `when service return error - then throw exception`(@TempDir projectDir: File) {
        generateProject(projectDir)
        mockDispatcher.registerMock(Mock(
            requestMatcher = { path.contains("getSchemaForPath") },
            response = MockResponse().setResponseCode(500)
        ))
        runTask(projectDir, apiPath = "/test/api", failure = true)
            .assertThat()
            .buildFailed()
            .outputContains("500")
    }

    @Test
    fun `when service return empty schemes - then throw that path not contains any schemes`(@TempDir projectDir: File) {
        generateProject(projectDir)
        mockDispatcher.registerMock(Mock(
            requestMatcher = { path.contains("getSchemaForPath") },
            response = apiSchemeEmptyResponse()
        ))

        val path = "/test/api"
        runTask(projectDir, apiPath = path, failure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Did not find any schemes for `$path`")
    }

    private fun TestResultSubject.outputContainsFilePaths(
        packageName: String,
        apiPath: String,
        paths: List<String>
    ): TestResultSubject {
        val packagePath = packageName.replace(".", "/")
        var result = this
        paths.forEach { result = result.outputContains("$packagePath/api-clients$apiPath/$it") }
        return result
    }

    private fun generateProject(
        projectDir: File,
        generatedClassesPackage: String = "com.example"
    ) {
        NetworkCodegenProjectGenerator.generate(
            projectDir = projectDir,
            serviceUrl = mockWebServer.url("/").toString(),
            generatedClassesPackage = generatedClassesPackage
        )
    }

    private fun runTask(
        tempDir: File,
        apiPath: String,
        failure: Boolean = false,
    ): TestResult {
        return gradlew(
            tempDir,
            "addEndpoint", "-PapiSchemesUrl=$apiPath",
            useTestFixturesClasspath = true,
            expectFailure = failure
        )
    }
}
