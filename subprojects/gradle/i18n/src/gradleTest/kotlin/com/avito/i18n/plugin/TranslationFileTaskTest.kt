package com.avito.i18n.plugin

import com.avito.android.tls.test.createMtlsExtensionString
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TranslationFileTaskTest {

    private val mockDispatcher = MockDispatcher()
    private val mockWebServer = MockWebServerFactory.create()
        .apply {
            dispatcher = mockDispatcher
        }

    @BeforeEach
    fun startup() {
        mockWebServer.start()
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `run update translations task without strings file - failed`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.i18n")
                    },
                    enableKotlinAndroidPlugin = false
                )
            )
        ).generateIn(projectDir)

        gradlew(
            projectDir = projectDir,
            ":app:${TranslationPlugin.TRANSLATION_TASK_NAME}",
            expectFailure = true
        ).assertThat()
            .buildFailed()
    }

    @Test
    fun `run update translations task with strings file - successful`(@TempDir projectDir: File) {
        val moduleName = "app"
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tls-configuration")
            },
            buildGradleExtra = """
                ${createMtlsExtensionString()}
            """.trimIndent(),
            modules = listOf(
                AndroidAppModule(
                    name = moduleName,
                    plugins = plugins {
                        id("com.avito.android.i18n")
                    },
                    buildGradleExtra = """
                        translation {
                            locales = ["en"]
                            sourceLocale = "en"
                            namespace = "android"
                            serviceUrl = "${mockWebServer.url("/")}"
                            translateUrlPath = "$PATH"
                            useTls = false
                        }
                    """.trimIndent(),
                    enableKotlinAndroidPlugin = false
                )
            ),
            useKts = true
        ).generateIn(projectDir)

        projectDir.module(moduleName) {
            dir(RES_PATH) {
                file(
                    name = TranslationPlugin.DEFAULT_STRING_FILE,
                    content = ORIGINAL_FILE_CONTENT
                )
            }
        }

        mockDispatcher.registerMock(
            Mock(
                requestMatcher = {
                    path.contains(PATH)
                },
                response = MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(RESPONSE_BODY)
            )
        )

        gradlew(projectDir, ":app:${TranslationPlugin.TRANSLATION_TASK_NAME}")
            .assertThat()
            .buildSuccessful()

        val enFile = File(projectDir, "app/${RES_PATH}values-en/strings.xml")

        assertThat(enFile.exists())
            .isTrue()
        assertThat(enFile.readText())
            .contains(TRANSLATED_FILE_CONTENT)
    }

    private companion object {
        const val RES_PATH = "src/main/res/"
        const val PATH = "test_translate"

        @Language("JSON")
        val RESPONSE_BODY = """
                {
                  "result": {
                    "data": {
                      "componentSlug": "",
                      "namespaceSlug": "",
                      "targetTextUnits": {
                        "en": {
                          "textUnits": [
                            {
                              "key": "some_string",
                              "other": "emos gnirts"
                            },
                            {
                              "key": "params_string",
                              "other": "smarap %s gnirts %d"
                            }
                          ]
                        }
                      }
                    },
                    "error": null
                  }
                }
                """.trimIndent()
    }
}
