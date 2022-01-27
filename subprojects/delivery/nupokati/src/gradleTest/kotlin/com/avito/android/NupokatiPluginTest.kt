package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class NupokatiPluginTest {

    @Test
    fun `configuration successful - without nupokati config provided`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.nupokati")
                    }
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "tasks").assertThat().buildSuccessful()
    }

    @TestFactory
    fun `dry run uploadCdBuildResult - triggers required tasks`(@TempDir projectDir: File): List<DynamicTest> {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.nupokati")

                        applyWithBuildscript(
                            buildscriptClasspath = "com.google.firebase:firebase-crashlytics-gradle:2.7.1",
                            pluginId = "com.google.firebase.crashlytics"
                        )
                    },
                    imports = listOf("import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension"),
                    useKts = true,
                    buildGradleExtra = """
                        |android {
                        |    buildTypes {
                        |        getByName("release") {
                        |            isMinifyEnabled = true
                        |            proguardFile("proguard.pro")
                        |            
                        |            (this as ExtensionAware).configure<CrashlyticsExtension> {
                        |                mappingFileUploadEnabled = true
                        |            }
                        |        }
                        |    }
                        |}
                        |""".trimMargin()
                )
            )
        ).generateIn(projectDir)

        val testResult = gradlew(
            projectDir,
            ":app:uploadCdBuildResultRelease",
            dryRun = true
        )
            .assertThat()
            .buildSuccessful()

        // todo assert that "deployToGooglePlayRelease" and "uploadCrashlyticsMappingFileRelease"
        //  not called because no deployment
        return listOf(
            "artifactoryBackupRelease",
            "packageReleaseBundle",
        ).map { taskName ->

            dynamicTest("$taskName is triggered") {
                testResult.tasksShouldBeTriggered(":app:$taskName")
            }
        }
    }
}
