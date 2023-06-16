package com.avito.tech_budget.module_types

import com.avito.tech_budget.utils.successResponse
import com.avito.test.gradle.gradlew
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModuleTypesIntegrationTest {

    @field:TempDir
    private lateinit var projectDir: File

    private val mockDispatcher = MockDispatcher(unmockedResponse = successResponse())
    private val mockWebServer = MockWebServerFactory.create()
        .apply {
            dispatcher = mockDispatcher
        }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun test() {
        ModuleTypesTestProject.generate(projectDir, mockWebServer.url("/").toString())
        val request = mockDispatcher.captureRequest { path.contains("dumpModuleTypes") }

        val build = gradlew(
            projectDir,
            ":uploadModuleTypes",
            "-Pcom.avito.android.tech-budget.enable=true",
            useTestFixturesClasspath = true
        )
        build.assertThat().buildSuccessful()

        request.checks.singleRequestCaptured()
            .bodyContains("""{"moduleName":":A","functionalType":"Library"}""")
            .bodyContains("""{"moduleName":":B","functionalType":"Public"}""")
            .bodyContains("""{"moduleName":":C","functionalType":"Impl"}""")
    }
}
