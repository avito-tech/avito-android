package com.avito.android.tls

import com.avito.android.tls.test.stubs.StubRawConfigurationData
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TlsConfigurationPluginTest {

    @Test
    fun `when providers not defined - then throw exception with suggestions to add tls providers`(
        @TempDir projectDir: File
    ) {
        generate(projectDir, providers = emptyList())
        runTask(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("Register providers by adding mTls configuration to you build.gradle file.")
    }

    @Test
    fun `when providers defined but credentials is not created - then throw exception with suggestions`(
        @TempDir projectDir: File
    ) {
        val suggestion = "test suggestion"
        generate(
            projectDir,
            helperText = suggestion
        )
        runTask(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains(suggestion)
    }

    @Test
    fun `when providers defined and credentials is created - then return success`(
        @TempDir projectDir: File
    ) {
        val crtContent = "crtContent"
        val keyContent = "keyContent"
        generate(
            projectDir,
            crtContent = crtContent,
            keyContent = keyContent,
        )

        runTask(projectDir)
            .assertThat()
            .buildSuccessful()
            .outputContains(keyContent)
            .outputContains(crtContent)
    }

    @Test
    fun `when provider has a fallback and content is wrong - then use fallback and return`(
        @TempDir projectDir: File
    ) {
        val keyContent = "keyContent2"
        val crtContent = "crtContent2"
        generate(
            projectDir,
            providers = listOf(
                StubRawConfigurationData(
                    name = "stub1",
                ),
                StubRawConfigurationData(
                    name = "stub2",
                    keyContent = keyContent,
                    crtContent = crtContent,
                )
            )
        )

        runTask(projectDir)
            .assertThat()
            .buildSuccessful()
            .outputContains(keyContent)
            .outputContains(crtContent)
    }

    private fun generate(
        projectDir: File,
        crtContent: String = "",
        keyContent: String = "",
        helperText: String = "",
    ) {
        generate(
            projectDir, listOf(
                StubRawConfigurationData(
                    crtContent = crtContent,
                    keyContent = keyContent,
                    helperText = helperText
                )
            )
        )
    }

    private fun generate(
        projectDir: File,
        providers: List<StubRawConfigurationData>
    ) {
        TestProjectGenerator(
            name = "rootapp",
            imports = listOf(
                "import com.avito.android.tls.extensions.configuration.RawContentTlsCredentialsConfiguration",
                "import com.avito.android.tls.TlsProjectCredentialsFactory",
                "import com.avito.android.tls.test.TestTask",
            ),
            plugins = plugins {
                id("com.avito.android.tls-configuration")
            },
            buildGradleExtra = """
                tls { 
                    credentials {
                        ${createTlsCredentialsProviders(providers)}
                    }
                } 
                
            """.trimIndent(),
            modules = listOf(
                KotlinModule(
                    name = "lib",
                    imports = listOf(
                        "import com.avito.android.tls.TlsConfigurationPlugin",
                        "import com.avito.android.tls.test.TestTask",
                    ),
                    buildGradleExtra = """
                        tasks.register<TestTask>("testTask") { 
                            credentialsFactory.set(TlsConfigurationPlugin.provideCredentialsService(rootProject))
                        }
                    """.trimIndent(),
                    useKts = true,
                )
            ),
            useKts = true,
        ).generateIn(projectDir)
    }

    private fun createTlsCredentialsProviders(
        stubProviders: List<StubRawConfigurationData>
    ): String {
        return stubProviders.joinToString(separator = "\n", transform = ::registerProvider)
    }

    private fun registerProvider(provider: StubRawConfigurationData): String {
        return """
            registerProvider<RawContentTlsCredentialsConfiguration>(
                "${provider.name}",
                RawContentTlsCredentialsConfiguration::class.java,
            ) { 
                crtContent.set("${provider.crtContent}")
                keyContent.set("${provider.keyContent}")
                helperText.set("${provider.helperText}")
            }
        """.trimIndent()
    }

    private fun runTask(
        tempDir: File,
        expectFailure: Boolean = false
    ): TestResult {
        return gradlew(
            tempDir,
            "testTask",
            useTestFixturesClasspath = true,
            expectFailure = expectFailure
        )
    }
}
