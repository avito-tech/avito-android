package com.avito.tech_budget.feature_toggles

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

internal class UploadFeatureTogglesTest {

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
    fun `upload FeatureToggles - request ok`(@TempDir projectDir: File) {
        generateProject(projectDir)

        val request = mockDispatcher.captureRequest { path.contains("dumpFeatureToggles") }

        uploadFeatureToggles(projectDir)
            .assertThat()
            .buildSuccessful()

        request.checks.singleRequestCaptured().jsonEquals(EXPECTED_FEATURE_TOGGLES_RESULT)
    }

    @Test
    fun `upload FeatureToggles - code ownership not applied - build failure`(@TempDir projectDir: File) {
        generateProject(projectDir, includeCodeOwnership = false)

        uploadFeatureToggles(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains(
                "You must apply plugin `com.avito.android.code-ownership` to the root project to run this task"
            )
    }

    @Test
    fun `upload FeatureToggles - FeatureToggles not configured - build failure`(@TempDir projectDir: File) {
        generateProject(projectDir, configureFeatureTogglesTask = false)

        uploadFeatureToggles(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
    }

    @Test
    fun `upload FeatureToggles - upload error - build failure`(@TempDir projectDir: File) {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("dumpFeatureToggles") },
                response = failureResponse()
            )
        )
        generateProject(projectDir)

        uploadFeatureToggles(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Upload Feature Toggles request failed")
    }

    private fun uploadFeatureToggles(
        projectDir: File,
        expectFailure: Boolean = false,
    ) =
        gradlew(
            projectDir,
            "uploadFeatureToggles",
            "-Pcom.avito.android.tech-budget.enable=true",
            expectFailure = expectFailure
        )

    private fun generateProject(
        projectDir: File,
        includeCodeOwnership: Boolean = true,
        configureFeatureTogglesTask: Boolean = true
    ) = TestProjectGenerator(
        plugins = plugins {
            id("com.avito.android.gradle-logger")
            if (includeCodeOwnership) id("com.avito.android.code-ownership")
            id("com.avito.android.tech-budget")
        },
        useKts = true,
        imports = listOf(
            "import org.gradle.api.DefaultTask",
            "import com.avito.android.tech_budget.feature_toggles.CollectProjectFeatureTogglesTask",
            "import org.gradle.api.tasks.TaskAction",
            "import java.io.File"
        ),
        buildGradleExtra = """
                ${if (includeCodeOwnership) FAKE_OWNERSHIP_EXTENSION else ""}
                techBudget {
                    ${dumpInfoExtension(mockWebServer.url("/").toString())}
                    ${if (configureFeatureTogglesTask) collectFeatureToggles() else ""}
                }
            """.trimIndent(),
        modules = listOf()
    ).generateIn(projectDir).also {
        if (configureFeatureTogglesTask) {
            projectDir.file("links.json", FEATURE_TOGGLES_JSON)
        }
    }

    private fun collectFeatureToggles() = """
        collectFeatureToggles {
            abstract class FakeCollectFeatureTogglesTask: DefaultTask(), CollectProjectFeatureTogglesTask { 
                
                @TaskAction
                fun collectFeatureToggles() {
                    //do nothing, file is generated already
                }
            } 
            project.tasks.register("fakeCollectFeatureToggles", FakeCollectFeatureTogglesTask::class.java) { 
                featureTogglesOutput.set(File("links.json"))
            } 
            collectProjectFeatureTogglesTaskName.set("fakeCollectFeatureToggles")
        }
    """.trimIndent()

    private companion object {

        @Language("json")
        private val EXPECTED_FEATURE_TOGGLES_RESULT = """
           {
               "dumpInfo":{
                  "commitHash":"123",
                  "commitDate":"2022-10-31",
                  "project":"avito"
               },
               "featureToggles": [
                 {
                      "defaultValue": "false",
                      "description": "Whether to send Log.e as non fatals to analytics",
                      "isRemote": true,
                      "key": "send_log_errors_non_fatal_analytics",
                      "owners": [ "Speed" ]
                 }
               ]  
           }
        """.trimIndent()

        @Language("json")
        private val FEATURE_TOGGLES_JSON = """
          [
            {
              "defaultValue": "false",
              "description": "Whether to send Log.e as non fatals to analytics",
              "isRemote": true,
              "key": "send_log_errors_non_fatal_analytics",
              "owners": [ "Speed" ]
            }
          ]     
        """.trimIndent()
    }
}
