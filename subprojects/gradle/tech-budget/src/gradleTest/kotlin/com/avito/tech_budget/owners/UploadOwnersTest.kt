package com.avito.tech_budget.owners

import com.avito.android.utils.FAKE_OWNERSHIP_EXTENSION
import com.avito.android.utils.FAKE_OWNERS_PROVIDER_EXTENSION
import com.avito.tech_budget.utils.dumpInfoExtension
import com.avito.tech_budget.utils.failureResponse
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
                "owners":[{"teamID":"SpeedID","teamName":"Speed","unitID":"SpeedID","unitName":"Speed"},
            """.trimIndent(),
            """
                {"teamID":"MessengerID","teamName":"Messenger","unitID":"MessengerID","unitName":"Messenger"}]
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
            id("com.avito.android.gradle-logger")
            id("com.avito.android.tech-budget")
            id("com.avito.android.tls-configuration")
            if (includeCodeOwnership) id("com.avito.android.code-ownership")
        },
        useKts = true,
        buildGradleExtra = """
                ${if (includeCodeOwnership) FAKE_OWNERSHIP_EXTENSION else ""}
                ${if (includeCodeOwnership && includeOwnersProvider) FAKE_OWNERS_PROVIDER_EXTENSION else ""}
                
                object AvitoTechBudgetOwnerMapper : com.avito.android.tech_budget.owners.TechBudgetOwnerMapper {
                        override fun map(
                            owner: com.avito.android.model.Owner
                       ): com.avito.android.tech_budget.owners.TechBudgetOwner {
                ${
            if (includeCodeOwnership) {
                "require(owner is FakeOwners) { \"Unknown type of owner\" }\n" +
                    " return com.avito.android.tech_budget.owners.TechBudgetOwner(\n" +
                    "    teamID = owner.id,\n" +
                    "    teamName = owner.name,\n" +
                    "    unitID = owner.id,\n" +
                    "    unitName = owner.name\n" +
                    ")"
            } else {
                "error(\"Not implemented\")"
            }
        }       
                            
                           
                        }
                }
                techBudget {
                    ${dumpInfoExtension(mockWebServer.url("/").toString())}
                    
                    collectOwners { 
                        techBudgetOwnerMapper.set(AvitoTechBudgetOwnerMapper)
                    }
                }
            """.trimIndent(),
    ).generateIn(projectDir)
}
