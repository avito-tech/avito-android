package com.avito.instrumentation.impact

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.AndroidLibModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths

internal class CopySymbolsToAssetsTaskTest {

    @Test
    fun test(@TempDir projectDir: File) {
        val testBuildType = "debug"

        TestProjectGenerator(
            plugins = listOf("com.avito.android.impact"),
            modules = listOf(
                AndroidAppModule(
                    "app",
                    plugins = listOf("com.avito.android.instrumentation-test-impact-analysis"),
                    dependencies = "implementation(project(\":lib\"))",
                    mutator = {
                        dir("src/main/res/layout") {
                            file(
                                name = "new_layout.xml",
                                content = """<TextView xmlns:android="http://schemas.android.com/apk/res/android"
                                    xmlns:tools="http://schemas.android.com/tools"
                                        android:id="@+id/some_new_id"
                                        android:layout_width="match_parent"
                                        android:layout_height="wrap_content"
                                />""".trimIndent()
                            )
                        }
                    }
                ),
                AndroidLibModule("lib")
            )
        ).generateIn(projectDir)

        gradlew(
            projectDir, "app:package${testBuildType.capitalize()}AndroidTest",
            "-Pci=true",
            "-PgitBranch"
        )
            .assertThat()
            .tasksShouldBeTriggered(
                ":app:merge${testBuildType.capitalize()}AndroidTestAssets",
                ":app:copy${testBuildType.capitalize()}SymbolsToAssets",
                ":app:package${testBuildType.capitalize()}AndroidTest"
            )

        val runtimeSymbolListInAssets = Paths.get(
            projectDir.path,
            "app",
            "build",
            "intermediates",
            "merged_assets",
            "${testBuildType}AndroidTest",
            "out",
            "R.txt"
        ).toFile()

        assertThat(runtimeSymbolListInAssets.readLines()).comparingElementsUsing(startWithCorrespondence)
            .containsAtLeast(
                "int id some_new_id",
                "int layout new_layout"
            )
    }
}
