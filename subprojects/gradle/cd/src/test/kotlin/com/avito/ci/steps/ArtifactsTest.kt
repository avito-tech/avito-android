package com.avito.ci.steps

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
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
                    plugins = listOf(
                        "com.avito.android.signer",
                        "com.avito.android.cd"
                    ),
                    customScript = """
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
            .buildFailed("already registered")
    }
}
