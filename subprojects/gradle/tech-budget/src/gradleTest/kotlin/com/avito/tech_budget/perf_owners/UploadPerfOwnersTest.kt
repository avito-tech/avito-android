package com.avito.tech_budget.perf_owners

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

internal class UploadPerfOwnersTest {

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
    fun `upload PerfOwners - request ok`(@TempDir projectDir: File) {
        generateProject(projectDir)

        val request = mockDispatcher.captureRequest { path.contains("dumpPerformanceScreenInfos") }

        uploadPerfOwners(projectDir)
            .assertThat()
            .buildSuccessful()

        request.checks.singleRequestCaptured().jsonEquals(SCREEN_OWNERS_REQUEST)
    }

    @Test
    fun `upload PerfOwners - request failed`(@TempDir projectDir: File) {
        generateProject(projectDir)

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("dumpPerformanceScreenInfos") },
                response = failureResponse()
            )
        )

        uploadPerfOwners(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Upload performance screen owners request failed")
    }

    @Test
    fun `upload PerfOwners - perf owners not configured - build failure`(@TempDir projectDir: File) {
        generateProject(projectDir, configurePerfOwnersTask = false)

        uploadPerfOwners(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
    }

    private fun uploadPerfOwners(
        projectDir: File,
        expectFailure: Boolean = false,
    ) =
        gradlew(
            projectDir,
            "uploadPerfScreensOwners",
            "-Pcom.avito.android.tech-budget.enable=true",
            expectFailure = expectFailure
        )

    private fun generateProject(
        projectDir: File,
        includeCodeOwnership: Boolean = true,
        configurePerfOwnersTask: Boolean = true
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
            "import com.avito.android.tech_budget.perf_screen_owners.CollectPerfOwnersTask",
            "import org.gradle.api.tasks.TaskAction",
            "import java.io.File"
        ),
        buildGradleExtra = """
                ${if (includeCodeOwnership) FAKE_OWNERSHIP_EXTENSION else ""}
                techBudget {
                    ${dumpInfoExtension(mockWebServer.url("/").toString())}
                    ${if (configurePerfOwnersTask) collectPerfOwners() else ""}
                }
            """.trimIndent(),
        modules = listOf()
    ).generateIn(projectDir).also {
        if (configurePerfOwnersTask) {
            projectDir.file("screen_owners.json", SCREEN_OWNERS_JSON)
        }
    }

    private fun collectPerfOwners() = """
        collectPerfOwners {
            abstract class FakeCollectPerfOwnersTask: DefaultTask(), CollectPerfOwnersTask { 
                
                @TaskAction
                fun collectABTests() {
                    //do nothing, file is generated already
                }
            } 
            project.tasks.register("fakeCollectPerfOwners", FakeCollectPerfOwnersTask::class.java) { 
                perfOwnersOutput.set(File("screen_owners.json"))
            } 
            collectProjectPerfOwnersTaskName.set("fakeCollectPerfOwners")

        }
    """.trimIndent()

    companion object {

        @Language("json")
        private val SCREEN_OWNERS_JSON = """
          [
            {
               "contentTypes": [
                   {
                       "description": "My desc of type",
                       "isIndirect": false,
                       "name": "My content type",
                       "owners": ["team_id2"]
                   }
               ],
               "description": "My feature screen",
               "name": "My screen name",
               "owners": ["team_id2", "team_id3"]
            }
          ]     
        """.trimIndent()

        @Language("json")
        private val SCREEN_OWNERS_REQUEST = """ 
            {
                "dumpInfo":{"commitHash":"123","commitDate":"2022-10-31","project":"avito"},
                "screenInfos": $SCREEN_OWNERS_JSON
            }
        """.trimIndent()
    }
}
