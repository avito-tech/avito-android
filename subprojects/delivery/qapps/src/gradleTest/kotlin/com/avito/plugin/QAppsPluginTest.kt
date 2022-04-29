package com.avito.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths

internal class QAppsPluginTest {

    @Test
    fun `plugin applied - with necessary parameters provided`(@TempDir projectDir: File) {
        createProject(
            projectDir,
            serviceUrl = "http://qapps.dev",
            branchName = "develop",
            comment = "build #1"
        )

        val result = gradlew(projectDir, "help")

        result.assertThat().buildSuccessful()
    }

    @Test
    fun `plugin apply fails - without required params - in ci`(@TempDir projectDir: File) {
        createProject(
            projectDir,
            serviceUrl = null,
            branchName = null,
            comment = null
        )

        val result = gradlew(projectDir, ":app:qappsUploadUnsignedDebug", expectFailure = true)

        result.assertThat().apply {
            buildFailed()
            outputContains("property 'branch' doesn't have a configured value")
            outputContains("property 'comment' doesn't have a configured value")
            outputContains("property 'host' doesn't have a configured value")
        }
    }

    private fun createProject(
        projectDir: File,
        serviceUrl: String?,
        comment: String?,
        branchName: String?
    ) {
        val stubApk = File(
            projectDir.toPath().resolve(Paths.get("app", "build", "outputs", "apk", "debug"))
                .toFile()
                .apply { mkdirs() },
            "app-debug.apk"
        ).apply { createNewFile() }

        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    "app",
                    enableKotlinAndroidPlugin = false,
                    imports = listOf(
                        "import com.avito.plugin.qappsUploadUnsignedTaskProvider"
                    ),
                    plugins = plugins {
                        id("com.avito.android.qapps")
                    },
                    buildGradleExtra = """
                        qapps {
                            ${serviceUrl.asOptionalPropertySetter("serviceUrl")}
                            ${comment.asOptionalPropertySetter("comment")}
                            ${branchName.asOptionalPropertySetter("branchName")}
                        }
                        
                        afterEvaluate {
                            tasks.qappsUploadUnsignedTaskProvider("debug").configure {
                                apkDirectory.set(file("${stubApk.parentFile}"))
                            }
                        }
                    """.trimIndent(),
                    useKts = true
                )
            )
        ).generateIn(projectDir)
    }

    private fun String?.asOptionalPropertySetter(name: String): String =
        if (this == null) "" else "$name.set(\"$this\")"
}
