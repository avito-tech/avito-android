package com.avito.tech_budget.owners

import com.avito.tech_budget.utils.dumpInfoExtension
import com.avito.tech_budget.utils.failureResponse
import com.avito.tech_budget.utils.ownershipExtension
import com.avito.tech_budget.utils.successResponse
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class UploadOwnersTest {

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
    fun `upload owners - owners sent to server`(@TempDir projectDir: File) {
        generateProject(projectDir)

        val request = mockDispatcher.captureRequest { path.contains("/dumpOwners") }

        uploadOwners(projectDir)
            .assertThat()
            .buildSuccessful()

        request.Checks().singleRequestCaptured().bodyContains(
            """
                "owners":[{"name":"Speed"},{"name":"Messenger"}]
            """.trimIndent()
        )
    }

    @Test
    fun `upload owners - code ownership not applied - build failure`(@TempDir projectDir: File) {
        generateProject(projectDir, includeCodeOwnership = false)

        uploadOwners(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("You must apply `com.avito.android.code-ownership` to run uploadOwners task!")
    }

    @Test
    fun `upload owners - ownersProvider not provided - build failure`(@TempDir projectDir: File) {
        generateProject(projectDir, includeOwnersProvider = false)

        uploadOwners(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
    }

    @Test
    fun `upload owners - upload error - build failure`(@TempDir projectDir: File) {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("/dumpOwners") },
                response = failureResponse()
            )
        )
        generateProject(projectDir)

        uploadOwners(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Upload owners request failed")
    }

    private fun uploadOwners(
        projectDir: File,
        expectFailure: Boolean = false,
    ) =
        gradlew(
            projectDir,
            "uploadOwners",
            "-Pcom.avito.android.tech-budget.enable=true",
            expectFailure = expectFailure
        )

    private fun generateProject(
        projectDir: File,
        includeCodeOwnership: Boolean = true,
        includeOwnersProvider: Boolean = true,
    ) = TestProjectGenerator(
        plugins = plugins {
            id("com.avito.android.tech-budget")
            if (includeCodeOwnership) id("com.avito.android.code-ownership")
        },
        useKts = true,
        buildGradleExtra = """
                ${if (includeCodeOwnership) ownershipExtension() else ""}
                ${if (includeCodeOwnership && includeOwnersProvider) codeOwnershipDiffExtension() else ""}
                techBudget {
                    ${dumpInfoExtension(mockWebServer.url("/").toString())}
                }
            """.trimIndent(),
    ).generateIn(projectDir)

    private fun codeOwnershipDiffExtension(): String = """
            codeOwnershipDiffReport {
                expectedOwnersProvider.set(
                    com.avito.android.diff.provider.OwnersProvider {
                        setOf(FakeOwners.Speed, FakeOwners.Messenger)                       
                    }
                )
            }
    """.trimIndent()
}
