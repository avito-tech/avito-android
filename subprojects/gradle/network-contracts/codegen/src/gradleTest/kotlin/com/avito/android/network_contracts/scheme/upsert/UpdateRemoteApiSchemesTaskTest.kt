package com.avito.android.network_contracts.scheme.upsert

import com.avito.android.network_contracts.DEFAULT_APP_NAME
import com.avito.android.network_contracts.DEFAULT_GENERATED_PACKAGE
import com.avito.android.network_contracts.NetworkCodegenProjectGenerator
import com.avito.android.network_contracts.defaultModule
import com.avito.android.network_contracts.scheme.fixation.collect.CollectApiSchemesTask
import com.avito.android.network_contracts.scheme.fixation.upsert.UpdateRemoteApiSchemesTask
import com.avito.android.network_contracts.scheme.imports.data.models.SchemaEntry
import com.avito.android.network_contracts.scheme.imports.mocks.apiSchemaImportResponseMock
import com.avito.android.network_contracts.scheme.imports.mocks.mockBase64ContentFile
import com.avito.android.network_contracts.scheme.upsert.mocks.generateExpectedJson
import com.avito.android.network_contracts.validation.ValidateNetworkContractsRootTask
import com.avito.git.Git
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.FolderModule
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.module.Module
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import com.avito.test.http.RequestData
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.io.File
import java.util.stream.Stream

internal class UpdateRemoteApiSchemesTaskTest {

    private val mockDispatcher = MockDispatcher(unmockedResponse = apiSchemaImportResponseMock())
    private val mockWebServer = MockWebServerFactory.create()
        .apply {
            dispatcher = mockDispatcher
        }

    private val upsertPathMatcher: RequestData.() -> Boolean = { path.contains("upsertClientVersion") }

    @TempDir
    lateinit var projectDir: File

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @BeforeEach
    fun startup() {
        mockWebServer.start()
    }

    @Test
    fun `when contracts fixation task is invoked - then order tasks is correct`(
        @TempDir projectDir: File
    ) {
        val moduleName = "feature"
        generateProject(projectDir, moduleName, disableValidationTask = false)
        runTask(projectDir, dryRun = true)
            .assertThat()
            .tasksShouldBeTriggered(
                ":${ValidateNetworkContractsRootTask.NAME}",
                ":$moduleName:${CollectApiSchemesTask.NAME}",
                ":${UpdateRemoteApiSchemesTask.NAME}"
            )
            .inOrder()
    }

    @ParameterizedTest
    @ArgumentsSource(BranchArgumentSource::class)
    fun `when schemes are exist and response is success - then upsert task is success`(
        branchName: String,
        expectedVersion: String,
    ) {
        val path = "test/api/1/scheme.yaml"
        val appName = "appName"
        val author = "test-author"
        val schemes = listOf(
            SchemaEntry(
                path = path,
                content = mockBase64ContentFile,
            )
        )

        mockDispatcher.registerDefaultMock(200)
        val request = mockDispatcher.captureRequest(upsertPathMatcher)

        val moduleFolderName = "feature"
        val moduleName = "impl"

        generateProject(
            projectDir = projectDir,
            modules = listOf(
                FolderModule(
                    name = moduleFolderName,
                    modules = upsertContractsModule(
                        name = moduleName,
                        appName = appName,
                    )
                )
            )
        )

        NetworkCodegenProjectGenerator.generateSchemes(
            projectDir = File(projectDir, "$moduleFolderName/$moduleName"),
            schemes = schemes
        )

        val git = Git.create(projectDir)
        git.checkout(branchName, create = true)

        runTask(projectDir, author)
            .assertThat()
            .buildSuccessful()
            .tasksShouldBeTriggered(":$moduleFolderName:$moduleName:${CollectApiSchemesTask.NAME}")

        val expectedJson = generateExpectedJson(
            moduleNotation = "${moduleFolderName}_$moduleName",
            author = author,
            appName = appName,
            version = expectedVersion,
            schemes = schemes
        )

        request.checks
            .singleRequestCaptured()
            .jsonEquals(expectedJson)
    }

    @Test
    fun `when schemes are exist and response is failure - then upsert task is failure with message`() {
        mockDispatcher.registerDefaultMock(500)

        val moduleName = "lib"
        generateProject(
            projectDir = projectDir,
            modules = defaultModule(moduleName)
        )

        NetworkCodegenProjectGenerator.generateSchemes(
            File(projectDir, moduleName),
            schemes = listOf(
                SchemaEntry(
                    path = "test/api/scheme.yaml",
                    content = mockBase64ContentFile,
                )
            ),
        )

        runTask(projectDir, failure = true)
            .assertThat()
            .buildFailed()
            .outputContains("<-- 500")
    }

    private fun generateProject(
        projectDir: File,
        moduleName: String = "upsert",
        generatedClassesPackage: String = DEFAULT_GENERATED_PACKAGE,
        disableValidationTask: Boolean = true,
        modules: List<Module> = upsertContractsModule(moduleName, generatedClassesPackage = generatedClassesPackage)
    ) {
        NetworkCodegenProjectGenerator.generate(
            projectDir = projectDir,
            serviceUrl = mockWebServer.url("/").toString(),
            generatedClassesPackage = generatedClassesPackage,
            modules = modules,
            buildExtra = disableValidationTask(projectDir).takeIf { disableValidationTask }.orEmpty()
        )
    }

    private fun upsertContractsModule(
        name: String = "upsert",
        appName: String = DEFAULT_APP_NAME,
        generatedClassesPackage: String = DEFAULT_GENERATED_PACKAGE,
    ): List<KotlinModule> {
        return defaultModule(
            name = name,
            appName = appName,
            generatedClassesPackage = generatedClassesPackage,
        )
    }

    private fun runTask(
        tempDir: File,
        author: String = "test-client",
        failure: Boolean = false,
        dryRun: Boolean = false,
    ): TestResult {
        return gradlew(
            tempDir,
            UpdateRemoteApiSchemesTask.NAME, "-Pavito.networkContracts.fixation.author=$author",
            useTestFixturesClasspath = true,
            expectFailure = failure,
            dryRun = dryRun
        )
    }

    private fun MockDispatcher.registerDefaultMock(code: Int) {
        registerMock(Mock(upsertPathMatcher, MockResponse().setResponseCode(code)))
    }

    private fun disableValidationTask(projectDir: File): String {
        val reportFile = File(projectDir, "reports/report.txt").apply {
            parentFile.mkdirs()
            createNewFile()
            writeText("OK")
        }
        return """
            tasks.named(
                "${UpdateRemoteApiSchemesTask.NAME}", 
                ${UpdateRemoteApiSchemesTask::class.qualifiedName}::class.java
            ).configure {
                validationReport.set(project.file("${reportFile.path}"))
            }
        """.trimIndent()
    }
}

private class BranchArgumentSource : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of("develop", "develop"),
            Arguments.of("release/165.0", "165.0"),
        )
    }
}
