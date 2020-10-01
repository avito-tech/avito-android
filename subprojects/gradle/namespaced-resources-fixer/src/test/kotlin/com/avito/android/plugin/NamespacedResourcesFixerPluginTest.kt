package com.avito.android.plugin

import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class NamespacedResourcesFixerPluginTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()
    }

    @Test
    fun `simple integration test`() {
        TestProjectGenerator(
            plugins = listOf(
                "com.avito.android.impact",
                "com.avito.android.namespaced-resources-fixer"
            ),
            modules = listOf(
                AndroidAppModule("app",
                dependencies = """
                    implementation project(':feature')
                """.trimIndent()
                ) {
                    file("src/androidTest/java/Test.kt", """
                        import com.app.R
                        val id = R.id.container
                    """.trimIndent())
                },
                AndroidLibModule("feature") {
                    file("src/main/res/layout/container.xml", """
                        <?xml version="1.0" encoding="utf-8"?>

                        <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
                            xmlns:app="http://schemas.android.com/apk/res-auto"
                            xmlns:tools="http://schemas.android.com/tools"
                            android:id="@+id/container"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content" />
                    """.trimIndent())
                }
            )
        ).generateIn(projectDir)

        val result = gradlew(
            projectDir, "fixNamespacedResources",
            "-Pavito.fixNamespacedResources.filesPrefix=app/src/androidTest",
            "-PgitBranch=xxx" // todo needed for impact plugin
        )
        result.assertThat().buildSuccessful()

        val newContent = File(projectDir, "app/src/androidTest/java/Test.kt").readText()
        assertThat(newContent).isEqualTo("""
            import com.app.R
            import com.feature.R as feature_R
            val id = feature_R.id.container
        """.trimIndent())
    }
}
