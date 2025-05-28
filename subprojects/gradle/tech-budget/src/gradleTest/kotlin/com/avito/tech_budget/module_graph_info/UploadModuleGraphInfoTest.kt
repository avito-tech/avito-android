package com.avito.tech_budget.module_graph_info

import com.avito.android.utils.FAKE_OWNERSHIP_EXTENSION
import com.avito.tech_budget.utils.dumpInfoExtension
import com.avito.tech_budget.utils.failureResponse
import com.avito.tech_budget.utils.successResponse
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class UploadModuleGraphInfoTest {

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
    fun `upload graph info - request ok`(@TempDir projectDir: File) {
        generateProject(projectDir)

        val request = mockDispatcher.captureRequest { path.contains("dumpModuleGraphInfo") }

        uploadModuleGraphInfo(projectDir)
            .assertThat()
            .buildSuccessful()

        request.checks.singleRequestCaptured().jsonEquals(MODULE_GRAPH_INFO_REQUEST)
    }

    @Test
    fun `upload graph info - request failed`(@TempDir projectDir: File) {
        generateProject(projectDir)

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("dumpModuleGraphInfo") },
                response = failureResponse()
            )
        )

        uploadModuleGraphInfo(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Upload Module Graph Info request failed")
    }

    private fun uploadModuleGraphInfo(
        projectDir: File,
        expectFailure: Boolean = false,
    ) =
        gradlew(
            projectDir,
            "uploadModuleGraphInfo",
            "-Pcom.avito.android.tech-budget.enable=true",
            expectFailure = expectFailure
        )

    private fun generateProject(
        projectDir: File,
    ) = TestProjectGenerator(
        plugins = plugins {
            id("com.avito.android.gradle-logger")
            id("com.avito.android.code-ownership")
            id("com.avito.android.tech-budget")
            id("com.avito.android.tls-configuration")
        },
        useKts = true,
        imports = listOf(
            "import org.gradle.api.DefaultTask",
            "import com.avito.android.module_graph.GenerateModuleGraphTask",
            "import com.avito.android.module_graph.ModuleGraphTask",
            "import org.gradle.api.tasks.TaskAction",
            "import java.io.File"
        ),
        buildGradleExtra = """
                $FAKE_OWNERSHIP_EXTENSION
                techBudget {
                    ${dumpInfoExtension(mockWebServer.url("/").toString())}
                    abstract class GenerateTask : DefaultTask(), ModuleGraphTask {
                            
                            @get:OutputFile
                            abstract override val outputFile: RegularFileProperty
                    }
                        project.tasks.register<GenerateTask>("generateModuleGraph") {
                            outputFile.set(project.file("module-graph.json"))
                        }
                }
            """.trimIndent(),
        modules = listOf(),
    ).generateIn(projectDir).also {
        projectDir.file("module-graph.json", MODULE_GRAPH_INFO_JSON)
    }

    companion object {

        @Language("json")
        private val MODULE_GRAPH_INFO_JSON = """
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
               ],
               "sizes": {
                    ":demo-app-1": 30,
                    ":demo-app-2": 20,
                    ":module-1": 10,
                    ":module-2": 10
               },
               "modulesToDemoApps": {
                    ":module-1": [
                      ":demo-app-1"
                    ],
                    ":module-2": [
                      ":demo-app-1", ":demo-app-2"
                    ]
               }
            }    
        """.trimIndent()

        @Language("json")
        private val MODULE_GRAPH_INFO_REQUEST = """ 
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
               ],
               "sizes": {
                    ":demo-app-1": 30,
                    ":demo-app-2": 20,
                    ":module-1": 10,
                    ":module-2": 10
               },
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
