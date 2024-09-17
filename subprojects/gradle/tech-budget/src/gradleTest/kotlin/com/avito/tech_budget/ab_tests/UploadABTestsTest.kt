package com.avito.tech_budget.ab_tests

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

internal class UploadABTestsTest {

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
    fun `upload ABTests - request ok`(@TempDir projectDir: File) {
        generateProject(projectDir)

        val request = mockDispatcher.captureRequest { path.contains("dumpABTests") }

        uploadABTests(projectDir)
            .assertThat()
            .buildSuccessful()

        request.checks.singleRequestCaptured().jsonEquals(EXPECTED_AB_TESTS_RESULT)
    }

    @Test
    fun `upload ABTests - code ownership not applied - build failure`(@TempDir projectDir: File) {
        generateProject(projectDir, includeCodeOwnership = false)

        uploadABTests(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains(
                "You must apply plugin `com.avito.android.code-ownership` to the root project to run this task"
            )
    }

    @Test
    fun `upload ABTests - ABTests not configured - build failure`(@TempDir projectDir: File) {
        generateProject(projectDir, configureABTestsTask = false)

        uploadABTests(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
    }

    @Test
    fun `upload ABTests - upload error - build failure`(@TempDir projectDir: File) {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("dumpABTests") },
                response = failureResponse()
            )
        )
        generateProject(projectDir)

        uploadABTests(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Upload AB Tests request failed")
    }

    private fun uploadABTests(
        projectDir: File,
        expectFailure: Boolean = false,
    ) =
        gradlew(
            projectDir,
            "uploadABTests",
            "-Pcom.avito.android.tech-budget.enable=true",
            expectFailure = expectFailure
        )

    private fun generateProject(
        projectDir: File,
        includeCodeOwnership: Boolean = true,
        configureABTestsTask: Boolean = true
    ) = TestProjectGenerator(
        plugins = plugins {
            id("com.avito.android.gradle-logger")
            if (includeCodeOwnership) id("com.avito.android.code-ownership")
            id("com.avito.android.tech-budget")
            id("com.avito.android.tls-configuration")
        },
        useKts = true,
        imports = listOf(
            "import org.gradle.api.DefaultTask",
            "import com.avito.android.tech_budget.ab_tests.CollectProjectABTestsTask",
            "import org.gradle.api.tasks.TaskAction",
            "import java.io.File"
        ),
        buildGradleExtra = """
                ${if (includeCodeOwnership) FAKE_OWNERSHIP_EXTENSION else ""}
                techBudget {
                    ${dumpInfoExtension(mockWebServer.url("/").toString())}
                    ${if (configureABTestsTask) collectABTests() else ""}
                }
                
            """.trimIndent(),
        modules = listOf()
    ).generateIn(projectDir).also {
        if (configureABTestsTask) {
            projectDir.file("links.json", AB_TESTS_JSON)
        }
    }

    private fun collectABTests() = """
        collectABTests {
            abstract class FakeCollectABTestsTask: DefaultTask(), CollectProjectABTestsTask { 
                
                @TaskAction
                fun collectABTests() {
                    //do nothing, file is generated already
                }
            } 
            project.tasks.register("fakeCollectABTests", FakeCollectABTestsTask::class.java) { 
                abTestsOutput.set(File("links.json"))
            } 
            collectProjectABTestsTaskName.set("fakeCollectABTests")
        }
    """.trimIndent()

    private companion object {

        @Language("json")
        private val EXPECTED_AB_TESTS_RESULT = """
           {
               "dumpInfo":{
                  "commitHash":"123",
                  "commitDate":"2022-10-31",
                  "project":"avito"
               },
               "abTests": [
                 {
                   "key": "new-main",
                   "defaultGroup": "control",
                   "groups": [ "test", "control" ],
                   "owners": [ "SpeedID"]
                 }
               ]  
           }
        """.trimIndent()

        @Language("json")
        private val AB_TESTS_JSON = """
          [
            {
               "key": "new-main",
               "defaultGroup": "control",
               "groups": [ "test", "control" ],
               "owners": [ "SpeedID"]
            }
          ]     
        """.trimIndent()
    }
}
