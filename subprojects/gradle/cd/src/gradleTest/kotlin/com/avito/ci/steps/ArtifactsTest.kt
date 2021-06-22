package com.avito.ci.steps

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ArtifactsTest {

    @Test
    fun `artifacts - fails on configuration - on duplicate ids`(@TempDir tempDir: File) {

        tempDir.file("1.json", "{}")
        tempDir.file("2.json", "{}")

        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "appA",
                    plugins = plugins {
                        id("com.avito.android.signer")
                        id("com.avito.android.cd")
                    },
                    buildGradleExtra = """
                            signService {
                                bundle(android.buildTypes.release, "no_matter")
                            }
                            builds {
                                release {
                                    artifacts {
                                        file("one","${'$'}{project.rootDir}/1.json")
                                        file("one","${'$'}{project.rootDir}/2.json")
                                    }
                                }
                            }
                        """.trimIndent()
                )
            )
        ).generateIn(tempDir)

        gradlew(tempDir, "appA:help", expectFailure = true).assertThat()
            .buildFailed()
            .outputContains("already registered")
    }
}
