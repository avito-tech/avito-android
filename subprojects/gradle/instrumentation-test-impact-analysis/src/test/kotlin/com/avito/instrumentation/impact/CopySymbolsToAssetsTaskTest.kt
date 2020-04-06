package com.avito.instrumentation.impact

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.AndroidLibModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths

/**
 * This test is higher level than [AndroidGradlePluginExtensionsKtTest],
 * will highlight ui impact analysis plugin errors on top of android gradle plugin contract changes
 */
internal class CopySymbolsToAssetsTaskTest {

    companion object {

        private const val testBuildType = "debug"
        private const val libPackageName = "some.lib.packagename"

        lateinit var projectDir: File

        @BeforeAll
        @JvmStatic
        fun prepareProject(@TempDir projectDir: File) {
            this.projectDir = projectDir

            TestProjectGenerator(
                plugins = listOf("com.avito.android.impact"),
                modules = listOf(
                    AndroidAppModule(
                        name = "app",
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
                    AndroidLibModule(
                        name = "lib",
                        packageName = libPackageName,
                        mutator = {
                            dir("src/main/res/layout") {
                                file(
                                    name = "some_lib_new_layout.xml",
                                    content = """<TextView xmlns:android="http://schemas.android.com/apk/res/android"
                                    xmlns:tools="http://schemas.android.com/tools"
                                        android:id="@+id/some_new_id_from_lib"
                                        android:layout_width="match_parent"
                                        android:layout_height="wrap_content"
                                />""".trimIndent()
                                )
                            }
                        }
                    )
                )
            ).generateIn(projectDir)

            gradlew(
                projectDir, "app:package${testBuildType.capitalize()}AndroidTest",
                "-Pci=true",
                "-PgitBranch"
            )
                .assertThat()
                .buildSuccessful()
                .tasksShouldBeTriggered(
                    ":app:merge${testBuildType.capitalize()}AndroidTestAssets",
                    ":app:copy${testBuildType.capitalize()}SymbolsToAssets",
                    ":app:package${testBuildType.capitalize()}AndroidTest"
                )
        }
    }

    @Test
    fun `copySymbolsToAssets - runtimeSymbolList of app contains its resources`() {
        val runtimeSymbolListInAssets = Paths.get(
            projectDir.path,
            "app",
            "build",
            "intermediates",
            "merged_assets",
            "${testBuildType}AndroidTest",
            "out",
            "impactAnalysisMeta",
            "R.txt"
        ).toFile()

        assertThat(runtimeSymbolListInAssets.readLines()).comparingElementsUsing(startWithCorrespondence)
            .containsAtLeast(
                "int id some_new_id",
                "int layout new_layout"
            )
    }

    @Test
    fun `copySymbolsToAssets - symbolList of library contains its resources and packageName`() {
        val libSymbolListInAssets = Paths.get(
            projectDir.path,
            "app",
            "build",
            "intermediates",
            "merged_assets",
            "${testBuildType}AndroidTest",
            "out",
            "impactAnalysisMeta",
            "lib_package-aware-r.txt"
        ).toFile()

        val libSymbolListInAssetsLines = libSymbolListInAssets.readLines()

        assertThat(libSymbolListInAssetsLines[0]).isEqualTo(libPackageName)
        assertThat(libSymbolListInAssetsLines).containsAtLeast(
            "id some_new_id_from_lib",
            "layout some_lib_new_layout"
        )
    }
}
