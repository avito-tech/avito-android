package com.avito.plugin

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

internal class QAppsPluginLegacyExtensionTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()

        val stubApk = File(
            tempPath.resolve(Paths.get("app", "build", "outputs", "apk", "debug"))
                .toFile()
                .apply { mkdirs() },
            "app-debug.apk"
        ).apply { createNewFile() }

        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    "app",
                    plugins = listOf("com.avito.android.qapps"),
                    buildGradleExtra = """
                        qapps {
                            host = "/"
                        }
                        afterEvaluate {
                            qappsUploadDebug {
                                apk = file("$stubApk")
                            }
                        }
                    """.trimIndent()
                )
            )
        ).generateIn(projectDir)
    }

    @Test
    fun `plugin applied - without gitState or envArgs - locally`() {
        val result = gradlew(projectDir, "-Pavito.build=local", "-Pavito.git.state=local", "help")

        result.assertThat().buildSuccessful()
    }

    @Test
    fun `plugin apply fails - without required params - in ci`() {
        val result =
            gradlew(projectDir, ":app:qappsUploadDebug", "-Pci=true", "-Pavito.git.state=env", expectFailure = true)

        result.assertThat().buildFailed()
    }
}
