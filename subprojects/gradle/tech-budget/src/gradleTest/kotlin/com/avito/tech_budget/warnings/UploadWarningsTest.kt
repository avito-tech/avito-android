package com.avito.tech_budget.warnings

import com.avito.tech_budget.warnings.CollectWarningsTest.Companion.WARNING_CONTENT
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dir
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class UploadWarningsTest {

    private val mockDispatcher = MockDispatcher(unmockedResponse = successResponse)
    private val mockWebServer = MockWebServerFactory.create()
        .apply {
            dispatcher = mockDispatcher
        }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `do not compile - logs information about empty warnings`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tech-budget")
            },
            buildGradleExtra = """
                techBudget {
                    ${dumpInfoConfig(mockWebServer.url("/"))}
                }
                """.trimIndent(),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.tech-budget")
                        id("com.avito.android.code-ownership")
                    }
                )
            )
        ).generateIn(projectDir)

        uploadWarnings(projectDir)
            .assertThat()
            .buildSuccessful()
            .outputContains("No warnings found")
    }

    @Test
    fun `compile without warnings - logs information about empty warnings`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tech-budget")
            },
            buildGradleExtra = """
                techBudget {
                    ${dumpInfoConfig(mockWebServer.url("/"))}
                }
                """.trimIndent(),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.tech-budget")
                        id("com.avito.android.code-ownership")
                    }
                )
            )
        ).generateIn(projectDir)

        uploadWarnings(projectDir).assertThat()
            .buildSuccessful()
            .outputContains("No warnings found")
    }

    @Test
    fun `compile with warnings - collection disabled - no warnings uploaded`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tech-budget")
            },
            buildGradleExtra = """
                techBudget {
                    ${dumpInfoConfig(mockWebServer.url("/"))}
                }
                """.trimIndent(),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.tech-budget")
                        id("com.avito.android.code-ownership")
                    },
                    mutator = {
                        dir("src/main/kotlin/") {
                            kotlinClass("DeprecatedClass") { WARNING_CONTENT }
                        }
                    }
                )
            )
        ).generateIn(projectDir)

        uploadWarnings(projectDir, collectWarningsEnabled = false)
            .assertThat()
            .buildSuccessful()
            .outputContains("No warnings found")
    }

    @Test
    fun `compile with warnings - uploadWarnings success`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tech-budget")
            },
            buildGradleExtra = """
                techBudget {
                    ${dumpInfoConfig(mockWebServer.url("/"))}
                }
                """.trimIndent(),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.tech-budget")
                        id("com.avito.android.code-ownership")
                    },
                    mutator = {
                        dir("src/main/kotlin/") {
                            kotlinClass("DeprecatedClass") { WARNING_CONTENT }
                        }
                    }
                )
            )
        ).generateIn(projectDir)

        uploadWarnings(projectDir)
            .assertThat()
            .buildSuccessful()
            .outputContains("Found 2 warnings")
    }

    @Test
    fun `compile with warnings - set owners - owners sent to server`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tech-budget")
            },
            buildGradleExtra = """
                techBudget {
                    ${dumpInfoConfig(mockWebServer.url("/"))}
                }
                """.trimIndent(),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.tech-budget")
                        id("com.avito.android.code-ownership")
                    },
                    mutator = {
                        dir("src/main/kotlin/") {
                            kotlinClass("DeprecatedClass") { WARNING_CONTENT }
                        }
                    },
                    useKts = true,
                    imports = listOf("import com.avito.android.model.Owner"),
                    buildGradleExtra = """
                        object Speed : Owner {
                           override fun toString(): String = "Speed"
                        }
                        object Performance : Owner { 
                           override fun toString(): String = "Performance"
                        }
                        
                        ownership {
                            owners(Speed, Performance)
                        }                    
                        """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        val request = mockDispatcher.captureRequest { path.contains("/dumpWarnings") }
        uploadWarnings(projectDir)
            .assertThat()
            .buildSuccessful()

        request.Checks().singleRequestCaptured().bodyContains(
            """
                "owners":["Speed","Performance"]
            """.trimIndent()
        )
    }

    @Test
    fun `compile with warnings - upload error - build failure`(@TempDir projectDir: File) {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains("/dumpWarnings") },
                response = failureResponse
            )
        )
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tech-budget")
            },
            buildGradleExtra = """
                techBudget {
                    ${dumpInfoConfig(mockWebServer.url("/"))}
                }
                """.trimIndent(),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.tech-budget")
                        id("com.avito.android.code-ownership")
                    },
                    mutator = {
                        dir("src/main/kotlin/") {
                            kotlinClass("DeprecatedClass") { WARNING_CONTENT }
                        }
                    }
                )
            )
        ).generateIn(projectDir)

        uploadWarnings(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Upload warnings request failed")
    }

    @Test
    fun `configure extension - warnings found`(@TempDir projectDir: File) {
        val separator = "***"
        val outputDirectoryName = "differentOutput"
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tech-budget")
            },
            buildGradleExtra = """
                techBudget {
                    ${dumpInfoConfig(mockWebServer.url("/"))}
                    
                    collectWarnings {
                        outputDirectory.set(project.file("$outputDirectoryName"))
                        warningsSeparator.set("$separator")
                    }
                }
            """.trimIndent(),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.tech-budget")
                        id("com.avito.android.code-ownership")
                    },
                    mutator = {
                        dir("src/main/kotlin/") {
                            kotlinClass("DeprecatedClass") { WARNING_CONTENT }
                        }
                    }
                )
            )
        ).generateIn(projectDir)

        uploadWarnings(projectDir)
            .assertThat()
            .buildSuccessful()
            .outputContains("Found 2 warnings")
    }

    private fun uploadWarnings(
        projectDir: File,
        expectFailure: Boolean = false,
        collectWarningsEnabled: Boolean = true
    ) =
        gradlew(
            projectDir,
            "uploadWarnings",
            "-Pcom.avito.android.tech-budget.enable=$collectWarningsEnabled",
            expectFailure = expectFailure
        )

    private companion object {
        val successResponse = MockResponse().setResponseCode(200).setBody(
            """
                {
                    "result": {
                        "id": "string"
                    }
                }
            """.trimIndent()
        )

        val failureResponse = MockResponse().setResponseCode(500).setBody(
            """
                {
                    "error": {
                        "kind": "validation",
                        "message": "string"
                    }
                }
            """.trimIndent()
        )

        fun dumpInfoConfig(baseUploadUrl: HttpUrl): String = """
            dumpInfo { 
                baseUploadUrl.set("$baseUploadUrl")
                commitHash.set("123")
                currentDate.set("2022-10-31")
                platform.set("android")
                project.set("avito")
            } 
        """.trimIndent()
    }
}
