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

class UploadModuleSizesTest {

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
    fun `upload module sizes - request ok`(@TempDir projectDir: File) {
        generateModuleGraphTestProject(projectDir, mockWebServer)

        val request = mockDispatcher.captureRequest { path.contains("dumpModuleSizes") }

        uploadModuleSizes(projectDir)
            .assertThat()
            .buildSuccessful()

        request.checks.singleRequestCaptured().jsonEquals(MODULE_SIZES_REQUEST)
    }

    @Test
    fun `upload module sizes - request failed`(@TempDir projectDir: File) {
        generateModuleGraphTestProject(projectDir, mockWebServer)

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("dumpModuleSizes") },
                response = failureResponse()
            )
        )

        uploadModuleSizes(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("UploadModuleSizes request failed")
    }

    private fun uploadModuleSizes(
        projectDir: File,
        expectFailure: Boolean = false,
    ) =
        gradlew(
            projectDir,
            "uploadModuleSizes",
            "-Pcom.avito.android.tech-budget.enable=true",
            expectFailure = expectFailure
        )

    companion object {

        @Language("json")
        private val MODULE_SIZES_REQUEST = """ 
            {
               "sizes": {
                    ":demo-app-1": 30,
                    ":demo-app-2": 20,
                    ":module-1": 10,
                    ":module-2": 10
               }
            }
        """.trimIndent()
    }
}
