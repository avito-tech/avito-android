package com.avito.android.string_transform

import com.avito.http.HttpCodes
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.MockWebServerFactory
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class StringTransformSigningAdapterGradleTest {

    private val mockWebServer = MockWebServerFactory.create()

    private lateinit var projectDir: File

    @BeforeEach
    fun setUp(@TempDir dir: File) {
        projectDir = dir
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `signing adapter - publishes signed transformed outputs - when signing is enabled and signer is configured`() {
        givenProject(
            buildGradleExtra = """
                android {
                    buildTypes {
                        release {
                            signingConfig = null
                        }
                    }
                }

                signer {
                    serviceUrl.set("${mockWebServer.url("/")}")
                    useTls.set(false)
                    apkSignTokens.put("com.example.app", "apk-token")
                    bundleSignTokens.put("com.example.app", "bundle-token")
                }

                transformStrings {
                    create("alpha") {
                        variant("release")
                        rules {
                            exact("samplevalue", "changedvalue")
                        }
                        integrations {
                            signing {
                                enabled.set(true)
                            }
                        }
                    }
                }
            """.trimIndent()
        ) { _ ->
            resolve("src/main/assets").mkdirs()
            resolve("src/main/assets/samplevalue.txt").writeText("samplevalue")
        }

        mockWebServer.enqueue(MockResponse().setResponseCode(HttpCodes.OK).setBody("SIGNED_CONTENT"))
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpCodes.OK).setBody("SIGNED_CONTENT"))

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val unsignedApk = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/apk/transformed-unsigned.apk"
        )
        val unsignedAab = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/aab/transformed-unsigned.aab"
        )
        val signedApk = File(
            projectDir,
            "app/build/outputs/signService/transformStrings/alpha/release/apk/transformed.apk"
        )
        val signedAab = File(
            projectDir,
            "app/build/outputs/signService/transformStrings/alpha/release/bundle/transformed.aab"
        )
        val canonicalSignedApk = File(
            projectDir,
            "app/build/outputs/signService/apk/release/app-release.apk"
        )
        val canonicalSignedAab = File(
            projectDir,
            "app/build/outputs/signService/bundle/release/app-release.aab"
        )

        assertThat(unsignedApk.exists()).isTrue()
        assertThat(unsignedAab.exists()).isTrue()
        assertThat(signedApk.exists()).isTrue()
        assertThat(signedAab.exists()).isTrue()
        assertThat(signedApk.readText()).isEqualTo("SIGNED_CONTENT")
        assertThat(signedAab.readText()).isEqualTo("SIGNED_CONTENT")
        assertThat(canonicalSignedApk.exists()).isFalse()
        assertThat(canonicalSignedAab.exists()).isFalse()
    }

    @Test
    fun `signing adapter - fails execution - when signer plugin is not applied`() {
        givenProject(
            buildGradleExtra = """
                transformStrings {
                    create("alpha") {
                        variant("release")
                        rules {
                            exact("samplevalue", "changedvalue")
                        }
                        integrations {
                            signing {
                                enabled.set(true)
                            }
                        }
                    }
                }
            """.trimIndent(),
            includeSignServicePlugin = false,
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
            expectFailure = true,
        ).assertThat()
            .buildFailed()
            .outputContains("String-transform pipeline 'alpha' enables signing adapter without SignServicePlugin")
    }

    @Test
    fun `signing adapter - does not register signing tasks - when signing integration is disabled`() {
        givenProject(
            buildGradleExtra = """
                transformStrings {
                    create("alpha") {
                        variant("release")
                        rules {
                            exact("samplevalue", "changedvalue")
                        }
                    }
                }
            """.trimIndent()
        )

        val output = gradlew(
            projectDir,
            ":app:tasks",
            "--all",
            useTestFixturesClasspath = true,
        ).output

        assertThat(output).doesNotContain("signTransformStringsAlphaReleaseApkViaService")
        assertThat(output).doesNotContain("signTransformStringsAlphaReleaseBundleViaService")
        assertThat(output).doesNotContain("validateTransformStringsAlphaReleaseSigningIntegration")
    }

    @Test
    fun `signing adapter - fails configuration - when mTLS is enabled without root tls plugin`() {
        givenProject(
            buildGradleExtra = """
                android {
                    buildTypes {
                        release {
                            signingConfig = null
                        }
                    }
                }

                signer {
                    serviceUrl.set("${mockWebServer.url("/")}")
                    apkSignTokens.put("com.example.app", "apk-token")
                    bundleSignTokens.put("com.example.app", "bundle-token")
                }

                transformStrings {
                    create("alpha") {
                        variant("release")
                        rules {
                            exact("samplevalue", "changedvalue")
                        }
                        integrations {
                            signing {
                                enabled.set(true)
                            }
                        }
                    }
                }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:tasks",
            useTestFixturesClasspath = true,
            expectFailure = true,
        ).assertThat()
            .buildFailed()
            .outputContains("Apply com.avito.android.tls-configuration plugin to the root project")
    }

    @Test
    fun `signing adapter - fails configuration - when required bundle token is missing`() {
        givenProject(
            buildGradleExtra = """
                android {
                    buildTypes {
                        release {
                            signingConfig = null
                        }
                    }
                }

                signer {
                    serviceUrl.set("${mockWebServer.url("/")}")
                    useTls.set(false)
                    apkSignTokens.put("com.example.app", "apk-token")
                }

                transformStrings {
                    create("alpha") {
                        variant("release")
                        rules {
                            exact("samplevalue", "changedvalue")
                        }
                        integrations {
                            signing {
                                enabled.set(true)
                            }
                        }
                    }
                }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:tasks",
            useTestFixturesClasspath = true,
            expectFailure = true,
        ).assertThat()
            .buildFailed()
            .outputContains("String-transform pipeline 'alpha' cannot enable signing for AAB")
            .outputContains("No bundle signing token is configured in signer for application id 'com.example.app'")
    }

    @Test
    fun `signing adapter - fails configuration - when required apk token is missing`() {
        givenProject(
            buildGradleExtra = """
                android {
                    buildTypes {
                        release {
                            signingConfig = null
                        }
                    }
                }

                signer {
                    serviceUrl.set("${mockWebServer.url("/")}")
                    useTls.set(false)
                    bundleSignTokens.put("com.example.app", "bundle-token")
                }

                transformStrings {
                    create("alpha") {
                        variant("release")
                        rules {
                            exact("samplevalue", "changedvalue")
                        }
                        integrations {
                            signing {
                                enabled.set(true)
                            }
                        }
                    }
                }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:tasks",
            useTestFixturesClasspath = true,
            expectFailure = true,
        ).assertThat()
            .buildFailed()
            .outputContains("String-transform pipeline 'alpha' cannot enable signing for APK")
            .outputContains("No APK signing token is configured in signer for application id 'com.example.app'")
    }

    private fun givenProject(
        buildGradleExtra: String,
        includeSignServicePlugin: Boolean = true,
        includeRootTlsPlugin: Boolean = false,
        appMutator: File.(AndroidAppModule) -> Unit = {},
    ) {
        TestProjectGenerator(
            plugins = plugins {
                if (includeRootTlsPlugin) {
                    id("com.avito.android.tls-configuration")
                }
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        if (includeSignServicePlugin) {
                            id("com.avito.android.sign-service")
                        }
                        id("com.avito.android.string-transform")
                    },
                    packageName = "com.example.app",
                    enableKotlinAndroidPlugin = false,
                    buildGradleExtra = buildGradleExtra,
                    useKts = true,
                    mutator = appMutator,
                )
            ),
            useKts = true,
        ).generateIn(projectDir)
    }
}
