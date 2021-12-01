
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.avito.test.http.MockWebServerFactory
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class ProsectorPluginTest {

    private val server = MockWebServerFactory.create()
    private val jsonRegex = Regex("(\\{.+})")

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `plugin generates tasks for default android variants`() {
        createTestAndroidProject(
            "app",
            "test.pkg",
            server.url("/"),
            "",
            "",
            "12.0"
        )
        val result = gradlew(":app:tasks")

        assertThat(result.output).contains("prosectorUploadDebug")
        assertThat(result.output).contains("prosectorUploadRelease")
    }

    @Test
    fun `prosectorUpload task - tolerate to unknown host exception`() {
        createTestAndroidProject(
            "app",
            "test.pkg",
            server.url("https://nonexisting.host"),
            "",
            "",
            "12.0"
        )

        val result = gradlew(":app:prosectorUploadDebug")

        result.assertThat().buildSuccessful()
    }

    @Test
    fun `release analysis meta passed correctly`() {
        createTestAndroidProject(
            "app",
            "test.pkg",
            server.url("/"),
            "MBS-2222",
            "356c1ad1dfb24e30fd0ffc4974892a34ff208859",
            "13.1"
        )

        server.enqueue(MockResponse().setBody("{ \"result\": \"ok\" }"))

        val result = gradlew(":app:prosectorUploadDebug")

        result.assertThat().buildSuccessful()
        val request = server.takeRequest()
        assertThat(request.path).isEqualTo("/tmct")

        val body = request.body.readUtf8()

        val found = jsonRegex.find(body)?.groupValues

        val meta = Gson().fromJson(found?.get(1), ReleaseAnalysisMeta::class.java)

        assertThat(meta.taskType).isEqualTo(TaskType.RELEASE_ANALYSIS)
        assertThat(meta.appPackage).isEqualTo("test.pkg.debug")
        assertThat(meta.buildInfo.branchName).isEqualTo("MBS-2222")
        assertThat(meta.buildInfo.commit).isEqualTo("356c1ad1dfb24e30fd0ffc4974892a34ff208859")
        assertThat(meta.buildInfo.versionName).isEqualTo("13.1")
        assertThat(meta.buildInfo.buildType).isEqualTo("debug")
    }

    private fun gradlew(vararg args: String): TestResult = com.avito.test.gradle.gradlew(projectDir, *args)

    private fun createTestAndroidProject(
        appModuleName: String,
        testPackageId: String,
        host: HttpUrl,
        branchName: String,
        commitHash: String,
        versionName: String
    ) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    appModuleName,
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.prosector")
                    },
                    versionName = versionName,
                    packageName = testPackageId,
                    buildGradleExtra = """
                         prosector {
                            host = "$host"
                            branchName = "$branchName"
                            commitHash = "$commitHash"
                         }
                         afterEvaluate {
                            prosectorUploadDebug {
                                apk = file("${'$'}buildDir/outputs/apk/debug/app-debug.apk")
                            }
                         }
                    """.trimIndent()
                )
            )
        ).generateIn(projectDir)
    }
}
