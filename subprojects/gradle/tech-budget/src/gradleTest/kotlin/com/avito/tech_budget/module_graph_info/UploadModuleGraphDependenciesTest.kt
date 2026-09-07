package com.avito.tech_budget.module_graph_info

import com.avito.tech_budget.utils.failureResponse
import com.avito.tech_budget.utils.successResponse
import com.avito.test.gradle.gradlew
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class UploadModuleGraphDependenciesTest {

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
    fun `upload graph dependencies - request ok`(@TempDir projectDir: File) {
        generateModuleGraphTestProject(projectDir, mockWebServer)

        val request = mockDispatcher.captureRequest { path.contains("dumpModuleGraphDependencies") }

        uploadModuleGraphDependencies(projectDir)
            .assertThat()
            .buildSuccessful()

        request.checks.singleRequestCaptured().jsonEquals(MODULE_GRAPH_DEPENDENCIES_REQUEST)
    }

    @Test
    fun `upload graph dependencies - request failed`(@TempDir projectDir: File) {
        generateModuleGraphTestProject(projectDir, mockWebServer)

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("dumpModuleGraphDependencies") },
                response = failureResponse()
            )
        )

        uploadModuleGraphDependencies(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("UploadModuleGraphDependencies request failed. HTTP 500")
            .outputContains("\"message\": \"string\"")
    }

    private fun uploadModuleGraphDependencies(
        projectDir: File,
        expectFailure: Boolean = false,
    ) =
        gradlew(
            projectDir,
            "uploadModuleGraphDependencies",
            "-Pcom.avito.android.tech-budget.enable=true",
            expectFailure = expectFailure
        )

    companion object {

        @Language("json")
        private val MODULE_GRAPH_DEPENDENCIES_REQUEST = """ 
            {
               "dependencies": [
                  {
                       "from": ":demo-app-1",
                       "to": ":module-1",
                       "type": "Implementation"
                   },
                   {
                       "from": ":demo-app-2",
                       "to": ":module-2",
                       "type": "Implementation"
                   },
                   {
                       "from": ":module-1",
                       "to": ":module-2",
                       "type": "Implementation"
                   }
               ]
            }
        """.trimIndent()
    }
}
