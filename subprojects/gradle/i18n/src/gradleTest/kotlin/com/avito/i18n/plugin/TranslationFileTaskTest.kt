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
                    name = MODULE_NAME,
                    plugins = plugins {
                        id("com.avito.android.i18n")
                    },
                    enableKotlinAndroidPlugin = false
                )
            )
        ).generateIn(projectDir)

        gradlew(
            projectDir = projectDir,
            ":$MODULE_NAME:${TranslationPlugin.TRANSLATION_TASK_NAME}",
            expectFailure = true
        ).assertThat()
            .buildFailed()
    }

    @Test
    fun `run update translations task with strings file - successful`(@TempDir projectDir: File) {
        generateTestProject(projectDir, ORIGINAL_FILE_CONTENT)

        createFakeResponse(RESPONSE_BODY)
        runTranslationTask(projectDir)

        val enFile = File(projectDir, "$MODULE_NAME/${MAIN_RES_PATH}values-en/strings.xml")

        assertThat(enFile.exists())
            .isTrue()
        assertThat(enFile.readText())
            .contains(TRANSLATED_FILE_CONTENT)
    }

    @Test
    fun `run update translations task with non-translatable string - successful`(@TempDir projectDir: File) {
        generateTestProject(projectDir, ORIGINAL_FILE_CONTENT_TRANSLATABLE_FALSE)
        createFakeResponse(RESPONSE_BODY_TRANSLATABLE_FALSE)
        runTranslationTask(projectDir)

        val enFile = File(projectDir, "app/${MAIN_RES_PATH}values-en/strings.xml")

        assertThat(enFile.exists())
            .isTrue()
        assertThat(enFile.readText())
            .contains(TRANSLATED_FILE_CONTENT_TRANSLATABLE_FALSE)
    }

    @Test
    fun `run update translations with string removed - successful`(@TempDir projectDir: File) {
        generateTestProject(projectDir, ORIGINAL_FILE_CONTENT_REMOVE_STRING)

        projectDir.module(MODULE_NAME) {
            dir(MAIN_RES_PATH) {
                file(
                    name = "values-en/strings.xml",
                    content = TRANSLATED_FILE_CONTENT
                )
            }
        }

        runTranslationTask(projectDir)

        val enFile = File(projectDir, "$MODULE_NAME/${MAIN_RES_PATH}values-en/strings.xml")

        assertThat(enFile.exists())
            .isTrue()
        assertThat(enFile.readText())
            .contains(TRANSLATED_FILE_CONTENT_REMOVE_STRING)
    }

    @Test
    fun `run update translations with changed string - successful`(@TempDir projectDir: File) {
        generateTestProject(projectDir, ORIGINAL_FILE_CONTENT_CHANGE_STRING)
        projectDir.module(MODULE_NAME) {
            dir(MAIN_RES_PATH) {
                file(
                    name = "values-en/strings.xml",
                    content = TRANSLATED_FILE_CONTENT
                )
            }
        }
        createFakeResponse(RESPONSE_BODY_CHANGE_STRING)
        runTranslationTask(projectDir)

        val enFile = File(projectDir, "app/${MAIN_RES_PATH}values-en/strings.xml")

        assertThat(enFile.exists())
            .isTrue()
        assertThat(enFile.readText())
            .contains(TRANSLATED_FILE_CONTENT_CHANGE_STRING)
    }

    @Test
    fun `run update translations with empty flavor config - translates only main flavor`(@TempDir projectDir: File) {
        generateTestProject(
            projectDir = projectDir,
            stringsFileContent = ORIGINAL_FILE_CONTENT,
            flavors = emptyList()
        )

        createFakeResponse(RESPONSE_BODY)
        runTranslationTask(projectDir)

        val mainFlavorEnFile = File(projectDir, "$MODULE_NAME/${MAIN_RES_PATH}values-en/strings.xml")

        assertThat(mainFlavorEnFile.exists())
            .isTrue()
        assertThat(mainFlavorEnFile.readText())
            .contains(TRANSLATED_FILE_CONTENT)

        val otherFlavorEnFile = File(projectDir, "$MODULE_NAME/${OTHER_RES_PATH}values-en/strings.xml")

        assertThat(otherFlavorEnFile.exists())
            .isFalse()
    }

    @Test
    fun `run update translations with other flavor in config - translates only other flavor`(
        @TempDir projectDir: File
    ) {
        generateTestProject(
            projectDir = projectDir,
            stringsFileContent = ORIGINAL_FILE_CONTENT,
            flavors = listOf("other")
        )

        createFakeResponse(RESPONSE_BODY)
        runTranslationTask(projectDir)

        val mainFlavorEnFile = File(projectDir, "$MODULE_NAME/${MAIN_RES_PATH}values-en/strings.xml")

        assertThat(mainFlavorEnFile.exists())
            .isFalse()

        val otherFlavorResPath = File(projectDir, "$MODULE_NAME/${OTHER_RES_PATH}values-en/strings.xml")

        assertThat(otherFlavorResPath.exists())
            .isTrue()
        assertThat(otherFlavorResPath.readText())
            .contains(TRANSLATED_FILE_CONTENT)
    }

    @Test
    fun `run update translations with unknown flavor - fails`(@TempDir projectDir: File) {
        generateTestProject(
            projectDir = projectDir,
            stringsFileContent = ORIGINAL_FILE_CONTENT,
            flavors = listOf("unknown")
        )

        gradlew(
            projectDir = projectDir,
            ":$MODULE_NAME:${TranslationPlugin.TRANSLATION_TASK_NAME}",
            expectFailure = true
        ).assertThat()
            .buildFailed()
    }

    @Test
    fun `run update translations with multiple flavors in config - translates all of them`(@TempDir projectDir: File) {
        generateTestProject(
            projectDir = projectDir,
            stringsFileContent = ORIGINAL_FILE_CONTENT,
            flavors = listOf("main", "other")
        )

        createFakeResponse(RESPONSE_BODY)
        runTranslationTask(projectDir)

        val mainFlavorEnFile = File(projectDir, "$MODULE_NAME/${MAIN_RES_PATH}values-en/strings.xml")

        assertThat(mainFlavorEnFile.exists())
            .isTrue()
        assertThat(mainFlavorEnFile.readText())
            .contains(TRANSLATED_FILE_CONTENT)

        val otherFlavorResPath = File(projectDir, "$MODULE_NAME/${OTHER_RES_PATH}values-en/strings.xml")

        assertThat(otherFlavorResPath.exists())
            .isTrue()
        assertThat(otherFlavorResPath.readText())
            .contains(TRANSLATED_FILE_CONTENT)
    }

    @Test
    fun `run update translations with empty string - keeps empty string in translated file`(@TempDir projectDir: File) {
        generateTestProject(
            projectDir = projectDir,
            stringsFileContent = ORIGINAL_FILE_CONTENT_WITH_EMPTY_STRINGS
        )

        createFakeResponse(RESPONSE_BODY)
        runTranslationTask(projectDir)

        val enFile = File(projectDir, "$MODULE_NAME/${MAIN_RES_PATH}values-en/strings.xml")

        assertThat(enFile.exists())
            .isTrue()
        assertThat(enFile.readText())
            .contains(TRANSLATED_FILE_CONTENT_WITH_EMPTY_STRINGS)
    }

    @Test
    fun `run update translations with markup string - sends markup and writes it back inline`(
        @TempDir projectDir: File
    ) {
        generateTestProject(projectDir, ORIGINAL_FILE_CONTENT_WITH_MARKUP)

        val requestCapturer = mockDispatcher.captureRequest { path.contains(PATH) }
        createFakeResponse(RESPONSE_BODY_WITH_MARKUP)
        runTranslationTask(projectDir)

        requestCapturer.checks.singleRequestCaptured()
            .bodyContains("<u>Подробнее</u>")

        val enFile = File(projectDir, "$MODULE_NAME/${MAIN_RES_PATH}values-en/strings.xml")

        assertThat(enFile.exists())
            .isTrue()
        assertThat(enFile.readText())
            .contains(TRANSLATED_FILE_CONTENT_WITH_MARKUP)
    }

    @Test
    fun `run update translations with CDATA string - sends content and keeps it literal`(
        @TempDir projectDir: File
    ) {
        generateTestProject(projectDir, ORIGINAL_FILE_CONTENT_WITH_CDATA)

        val requestCapturer = mockDispatcher.captureRequest { path.contains(PATH) }
        createFakeResponse(RESPONSE_BODY_WITH_CDATA)
        runTranslationTask(projectDir)

        requestCapturer.checks.singleRequestCaptured()
            .bodyContains("<b>Текст</b>")

        val enFile = File(projectDir, "$MODULE_NAME/${MAIN_RES_PATH}values-en/strings.xml")
        val translated = enFile.readText()

        assertThat(translated).contains("&lt;b&gt;Matn&lt;/b&gt;")
        assertThat(translated).doesNotContain("<b>Matn</b>")
    }

    private fun generateTestProject(
        @TempDir projectDir: File,
        stringsFileContent: String,
        flavors: List<String> = emptyList()
    ) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tls-configuration")
            },
            buildGradleExtra = """
                ${createMtlsExtensionString()}
            """.trimIndent(),
            modules = listOf(
                AndroidAppModule(
                    name = MODULE_NAME,
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
                            ${createTranslationExtensionString(flavors)}
                        }
                        
                        android {
                            flavorDimensions += "some_flavor"
                            
                            productFlavors {
                                create("other") { flavor ->
                                    flavor.dimension = "some_flavor"
                                }
                            }
                        }
                    """.trimIndent(),
                    enableKotlinAndroidPlugin = false
                ) {
                    dir(MAIN_RES_PATH) {
                        file(
                            name = TranslationPlugin.DEFAULT_STRING_FILE,
                            content = stringsFileContent
                        )
                    }
                    dir(OTHER_RES_PATH) {
                        file(
                            name = TranslationPlugin.DEFAULT_STRING_FILE,
                            content = stringsFileContent
                        )
                    }
                }
            ),
            useKts = true
        ).generateIn(projectDir)
    }

    private fun createFakeResponse(responseBody: String) {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = {
                    path.contains(PATH)
                },
                response = MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(responseBody)
            )
        )
    }

    private fun runTranslationTask(@TempDir projectDir: File) {
        gradlew(projectDir, ":$MODULE_NAME:${TranslationPlugin.TRANSLATION_TASK_NAME}")
            .assertThat()
            .buildSuccessful()
    }

    private fun createTranslationExtensionString(
        flavors: List<String> = emptyList()
    ): String = flavors.joinToString("\n") { "flavorNamesToTranslate.add(\"$it\")" }

    private companion object Companion {
        const val MAIN_RES_PATH = "src/main/res/"
        const val OTHER_RES_PATH = "src/other/res/"
        const val PATH = "test_translate"

        const val MODULE_NAME: String = "app"
    }
}
