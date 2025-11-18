package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class NupokatiPluginV4Test {

    @Test
    fun `configuration successful - without nupokati config provided`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.qapps")
                        id("com.avito.android.nupokati")
                    }
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "tasks").assertThat().buildSuccessful()
    }

    @Test
    fun `configuration successful - with v4 pipeline configured`(@TempDir projectDir: File) {
        val cdConfig = """
            |{
            |  "schema_version": 2,
            |  "project": "avito",
            |  "release_version": "1.0.0",
            |  "output_descriptor": {
            |    "path": "http://localhost/release_info.json",
            |    "skip_upload": false
            |  },
            |  "deployments": []
            |}""".trimMargin()

        val cdConfigFile = File(projectDir, "cd-config.json").also { it.writeText(cdConfig) }

        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.qapps")
                        id("com.avito.android.nupokati")
                    },
                    useKts = true,
                    imports = listOf(
                        "import com.avito.reportviewer.model.ReportCoordinates",
                        "import com.avito.android.gradle_configuration.extension.ArtifactV4"
                    ),
                    buildGradleExtra = """
                        |qapps {
                        |    branchName.set("develop")
                        |    comment.set("stub comment")
                        |    serviceUrl.set("http://localhost/")
                        |}
                        |
                        |nupokati {
                        |    cdBuildConfigFile.set(rootProject.file("${cdConfigFile.name}"))
                        |
                        |    v4("releaseV4") {
                        |        nupokatiUrl.set("http://localhost:8080/")
                        |
                        |        artifacts.set(
                        |           listOf(
                        |               ArtifactV4.Artifact(
                        |                   file = provider {
                        |                    projectDir.toPath().resolve("test.apk") 
                        |                   }
                        |               )
                        |           )
                        |        )
                        |
                        |        reportViewer {
                        |            frontendUrl.set("https://rv.example.com")
                        |            reportCoordinates.set(ReportCoordinates("Plan", "Job", "run-1"))
                        |        }
                        |    }
                        |}
                        |
                        |tasks.register("nupokati") {
                        |   dependsOn("nupokatiReleaseV4")
                        |}
                        |""".trimMargin()
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "tasks").assertThat().buildSuccessful()
    }
}
