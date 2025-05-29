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

class UploadModuleGraphAppDependenciesTest {

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
    fun `upload graph app dependencies - request ok`(@TempDir projectDir: File) {
        generateModuleGraphTestProject(projectDir, mockWebServer)

        val request = mockDispatcher.captureRequest { path.contains("dumpModuleGraphAppDependencies") }

        uploadModuleGraphAppDependencies(projectDir)
            .assertThat()
            .buildSuccessful()

        request.checks.singleRequestCaptured().jsonEquals(MODULE_GRAPH_APP_DEPENDENCIES_REQUEST)
    }

    @Test
    fun `upload graph app dependencies - request failed`(@TempDir projectDir: File) {
        generateModuleGraphTestProject(projectDir, mockWebServer)

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("dumpModuleGraphAppDependencies") },
                response = failureResponse()
            )
        )

        uploadModuleGraphAppDependencies(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("UploadModuleGraphAppDependencies request failed")
    }

    private fun uploadModuleGraphAppDependencies(
        projectDir: File,
        expectFailure: Boolean = false,
    ) =
        gradlew(
            projectDir,
            "uploadModuleGraphAppDependencies",
            "-Pcom.avito.android.tech-budget.enable=true",
            expectFailure = expectFailure
        )

    companion object {

        @Language("json")
        private val MODULE_GRAPH_APP_DEPENDENCIES_REQUEST = """ 
            {
               "modulesToDemoApps": [
                    {
                       "module": ":module-1",
                       "demoApp": ":demo-app-1"
                    },
                    {
                       "module": ":module-2",
                       "demoApp": ":demo-app-1"
                    },
                    {
                       "module": ":module-2",
                       "demoApp": ":demo-app-2"
                    }
               ]
            }
        """.trimIndent()
    }
}
