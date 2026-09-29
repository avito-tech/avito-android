package com.avito.i18n.plugin

import com.avito.android.tls.test.createMtlsExtensionString
import com.avito.android.tls.test.stubs.StubRawConfigurationData
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.Mock
import com.avito.test.http.MockDispatcher
import com.avito.test.http.MockWebServerFactory
import okhttp3.mockwebserver.MockResponse
import okhttp3.tls.HeldCertificate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.concurrent.TimeUnit

class InstantFeedbackIntegrationTest {

    private val mockDispatcher = MockDispatcher()
    private val mockWebServer = MockWebServerFactory.create()
        .apply { dispatcher = mockDispatcher }

    @BeforeEach
    fun startup() {
        mockWebServer.start()
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `translations updated - event sent with login from certificate`(@TempDir projectDir: File) {
        generateTestProject(projectDir)
        registerTranslationResponse()
        val feedback = registerFeedbackEndpoint()

        runTranslationTask(projectDir)

        feedback.checks.singleRequestCaptured()
            .bodyContains(""""scenario":"after_android_update_translations"""")
            .bodyContains(""""user":"$LOGIN"""")
            .bodyContains(""""status":"success"""")
            .bodyContains(""""target":":$MODULE_NAME"""")
    }

    @Test
    fun `write of translated file fails - failure event still sent`(@TempDir projectDir: File) {
        generateTestProject(projectDir)
        registerTranslationResponse()
        val feedback = registerFeedbackEndpoint()

        // the locale directory is taken by a file: writing fails after the backend has replied
        File(projectDir, "$MODULE_NAME/${MAIN_RES_PATH}values-en").writeText("not a directory")

        gradlew(
            projectDir,
            ":$MODULE_NAME:${TranslationPlugin.TRANSLATION_TASK_NAME}",
            "-Pavito.instantFeedback.enabled=true",
            "-Pavito.instantFeedback.endpoint=${mockWebServer.url("/$FEEDBACK_PATH")}",
            expectFailure = true,
        ).assertThat().buildFailed()

        feedback.checks.singleRequestCaptured()
            .bodyContains("\"status\":\"failure\"")
    }

    @Test
    fun `nothing to translate - no event`(@TempDir projectDir: File) {
        generateTestProject(projectDir, stringsFileContent = EMPTY_STRINGS)
        registerTranslationResponse()
        val feedback = registerFeedbackEndpoint()

        runTranslationTask(projectDir)

        feedback.checks.nothingCaptured()
    }

    @Test
    fun `feedback disabled explicitly - no event`(@TempDir projectDir: File) {
        generateTestProject(projectDir)
        registerTranslationResponse()
        val feedback = registerFeedbackEndpoint()

        runTranslationTask(projectDir, feedbackEnabled = false)

        feedback.checks.nothingCaptured()
    }

    @Test
    fun `service certificate instead of personal - build fails`(@TempDir projectDir: File) {
        generateTestProject(projectDir, certificate = serviceCertificate())
        registerTranslationResponse()
        val feedback = registerFeedbackEndpoint()

        runTranslationTask(projectDir, expectFailure = true)
            .assertThat()
            .outputContains("mTLS certificate (no personal certificate among")

        feedback.checks.nothingCaptured()
    }

    @Test
    fun `feedback endpoint answers with error - build fails with the reason`(@TempDir projectDir: File) {
        generateTestProject(projectDir)
        registerTranslationResponse()
        val feedback = registerFailingFeedbackEndpoint()

        runTranslationTask(projectDir, expectFailure = true)
            .assertThat()
            .outputContains("responded with 500: . Turn the feature off")

        feedback.checks.singleRequestCaptured()
    }

    @Test
    fun `translation failed and event not sent - original error survives`(@TempDir projectDir: File) {
        generateTestProject(projectDir)
        registerFailingTranslationResponse()
        val feedback = registerFailingFeedbackEndpoint()

        runTranslationTask(projectDir, expectFailure = true)
            .assertThat()
            .outputContains("Translation API request failed")
            // the send failure is demoted to a warning, so the build fails once and for the right reason
            .outputContains("Instant feedback: event not sent")
            .outputDoesNotContain("Build completed with 2 failures")

        feedback.checks.singleRequestCaptured()
    }

    @Test
    fun `dry run with unusable certificate - nothing sent and build is green`(@TempDir projectDir: File) {
        generateTestProject(projectDir, certificate = serviceCertificate())
        registerTranslationResponse()
        val feedback = registerFeedbackEndpoint()

        gradlew(
            projectDir,
            ":$MODULE_NAME:${TranslationPlugin.TRANSLATION_TASK_NAME}",
            "-Pavito.instantFeedback.enabled=true",
            "-Pavito.instantFeedback.dryRun=true",
            "-Pavito.instantFeedback.endpoint=${mockWebServer.url("/$FEEDBACK_PATH")}",
        ).assertThat().buildSuccessful()

        feedback.checks.nothingCaptured()
    }

    private fun registerFeedbackEndpoint() = mockDispatcher
        .also {
            it.registerMock(
                Mock(
                    requestMatcher = { path.contains(FEEDBACK_PATH) },
                    response = MockResponse().setResponseCode(200)
                )
            )
        }
        .captureRequest { path.contains(FEEDBACK_PATH) }

    private fun registerFailingFeedbackEndpoint() = mockDispatcher
        .also {
            it.registerMock(
                Mock(
                    requestMatcher = { path.contains(FEEDBACK_PATH) },
                    response = MockResponse().setResponseCode(500),
                )
            )
        }
        .captureRequest { path.contains(FEEDBACK_PATH) }

    private fun registerFailingTranslationResponse() {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains(TRANSLATE_PATH) },
                response = MockResponse().setResponseCode(500),
            )
        )
    }

    private fun registerTranslationResponse() {
        mockDispatcher.registerMock(
            Mock(
                requestMatcher = { path.contains(TRANSLATE_PATH) },
                response = MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(RESPONSE_BODY)
            )
        )
    }

    private fun runTranslationTask(
        projectDir: File,
        feedbackEnabled: Boolean = true,
        expectFailure: Boolean = false,
    ): TestResult = gradlew(
        projectDir,
        ":$MODULE_NAME:${TranslationPlugin.TRANSLATION_TASK_NAME}",
        "-Pavito.instantFeedback.enabled=$feedbackEnabled",
        "-Pavito.instantFeedback.endpoint=${mockWebServer.url("/$FEEDBACK_PATH")}",
        expectFailure = expectFailure,
    ).also { if (!expectFailure) it.assertThat().buildSuccessful() }

    private fun generateTestProject(
        projectDir: File,
        stringsFileContent: String = ORIGINAL_STRINGS,
        certificate: HeldCertificate = personalCertificate(),
    ) {
        val mtlsProvider = StubRawConfigurationData(
            crtContent = certificate.certificatePem().escapeForBuildScript(),
            keyContent = "keyContent",
        )

        TestProjectGenerator(
            plugins = plugins { id("com.avito.android.tls-configuration") },
            buildGradleExtra = createMtlsExtensionString(listOf(mtlsProvider)),
            modules = listOf(
                AndroidAppModule(
                    name = MODULE_NAME,
                    plugins = plugins { id("com.avito.android.i18n") },
                    buildGradleExtra = """
                        translation {
                            locales = ["en"]
                            sourceLocale = "en"
                            namespace = "android"
                            serviceUrl = "${mockWebServer.url("/")}"
                            translateUrlPath = "$TRANSLATE_PATH"
                            useTls = false
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
                }
            ),
            useKts = true
        ).generateIn(projectDir)
    }

    private fun personalCertificate(): HeldCertificate = HeldCertificate.Builder()
        .commonName(LOGIN)
        .validityInterval(
            System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
            System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30),
        )
        .signedBy(certificateAuthority(PERSONAL_CA))
        .build()

    private fun serviceCertificate(): HeldCertificate = HeldCertificate.Builder()
        .commonName("some-service-proxy")
        .validityInterval(
            System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
            System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30),
        )
        .signedBy(certificateAuthority("some-intermediate-ca"))
        .build()

    private fun certificateAuthority(commonName: String): HeldCertificate = HeldCertificate.Builder()
        .commonName(commonName)
        .certificateAuthority(0)
        .build()

    private fun String.escapeForBuildScript(): String = replace("\n", "\\n")

    private companion object {
        const val MAIN_RES_PATH = "src/main/res/"
        const val TRANSLATE_PATH = "test_translate"
        const val FEEDBACK_PATH = "sendEvent"
        const val MODULE_NAME = "app"
        const val LOGIN = "testuser"

        const val PERSONAL_CA = "personal-ca-0000"

        val ORIGINAL_STRINGS = """
            <resources>
                <string name="test">Привет</string>
            </resources>
        """.trimIndent()

        val EMPTY_STRINGS = """
            <resources>
            </resources>
        """.trimIndent()

        val RESPONSE_BODY = """
            {
              "result": {
                "status": { "code": "ok", "message": "" },
                "data": {
                  "task": {
                    "taskUUID": "uuid",
                    "namespace": "android",
                    "contentType": "text",
                    "region": null,
                    "status": "done",
                    "description": ""
                  },
                  "textUnits": [
                    {
                      "status": 10,
                      "unit": {
                        "key": "test",
                        "component": ":$MODULE_NAME",
                        "namespace": "android",
                        "status": 10,
                        "translated": [ { "lang": "en", "text": { "text": "Hello" } } ],
                        "error": null
                      },
                      "absentLanguages": null
                    }
                  ]
                }
              }
            }
        """.trimIndent()
    }
}
