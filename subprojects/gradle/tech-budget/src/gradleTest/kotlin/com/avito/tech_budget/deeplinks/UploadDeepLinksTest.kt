package com.avito.tech_budget.deeplinks

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

internal class UploadDeepLinksTest {

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
    fun `upload deepLinks - request ok`(@TempDir projectDir: File) {
        generateProject(projectDir)

        val request = mockDispatcher.captureRequest { path.contains("dumpDeepLinks") }

        uploadDeepLinks(projectDir)
            .assertThat()
            .buildSuccessful()

        request.checks.singleRequestCaptured().jsonEquals(EXPECTED_DEEP_LINKS_RESULT)
    }

    @Test
    fun `upload deepLinks - code ownership not applied - build failure`(@TempDir projectDir: File) {
        generateProject(projectDir, includeCodeOwnership = false)

        uploadDeepLinks(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains(
                "You must apply plugin `com.avito.android.code-ownership` to the root project to run this task"
            )
    }

    @Test
    fun `upload deepLinks - deepLinks not configured - build failure`(@TempDir projectDir: File) {
        generateProject(projectDir, configureDeeplinksTask = false)

        uploadDeepLinks(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
    }

    @Test
    fun `upload deepLinks - upload error - build failure`(@TempDir projectDir: File) {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("dumpDeepLinks") },
                response = failureResponse()
            )
        )
        generateProject(projectDir)

        uploadDeepLinks(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Upload deepLinks request failed")
    }

    private fun uploadDeepLinks(
        projectDir: File,
        expectFailure: Boolean = false,
    ) =
        gradlew(
            projectDir,
            "uploadDeepLinks",
            "-Pcom.avito.android.tech-budget.enable=true",
            expectFailure = expectFailure
        )

    private fun generateProject(
        projectDir: File,
        includeCodeOwnership: Boolean = true,
        configureDeeplinksTask: Boolean = true
    ) = TestProjectGenerator(
        plugins = plugins {
            id("com.avito.android.gradle-logger")
            if (includeCodeOwnership) id("com.avito.android.code-ownership")
            id("com.avito.android.tech-budget")
        },
        useKts = true,
        imports = listOf(
            "import org.gradle.api.DefaultTask",
            "import com.avito.android.tech_budget.deeplinks.CollectProjectDeeplinksTask",
            "import org.gradle.api.tasks.TaskAction",
            "import java.io.File"
        ),
        buildGradleExtra = """
                ${if (includeCodeOwnership) FAKE_OWNERSHIP_EXTENSION else ""}
                techBudget {
                    ${dumpInfoExtension(mockWebServer.url("/").toString())}
                    ${if (configureDeeplinksTask) collectDeepLinks() else ""}
                }
            """.trimIndent(),
        modules = listOf()
    ).generateIn(projectDir).also {
        if (configureDeeplinksTask) {
            projectDir.file("links.json", DEEP_LINKS_JSON)
        }
    }

    private fun collectDeepLinks() = """
        collectDeepLinks {
            abstract class FakeCollectDeeplinksTask: DefaultTask(), CollectProjectDeeplinksTask { 
                
                @TaskAction
                fun collectDeeplinks() {
                    //do nothing, file is generated already
                }
            } 
            project.tasks.register("fakeCollectDeepLinks", FakeCollectDeeplinksTask::class.java) { 
                deeplinksOutput.set(File("links.json"))
            } 
            collectProjectDeeplinksTaskName.set("fakeCollectDeepLinks")
        }
    """.trimIndent()

    private companion object {

        @Language("json")
        private val EXPECTED_DEEP_LINKS_RESULT = """
           {
               "dumpInfo":{
                  "commitHash":"123",
                  "commitDate":"2022-10-31",
                  "project":"avito"
               },
               "deepLinks": [
                 {
                     "deepLinkName": "MainLink",
                     "moduleName": ":avito-app:main",
                     "path": "/main",
                     "version": 1,
                     "owners": ["Messenger"]
                 }
               ]  
           }
        """.trimIndent()

        @Language("json")
        private val DEEP_LINKS_JSON = """
          [
              {
                  "deepLinkName": "MainLink",
                  "moduleName": ":avito-app:main",
                  "path": "/main",
                  "version": 1,
                  "owners": ["Messenger"]
              }
          ]     
        """.trimIndent()
    }
}
