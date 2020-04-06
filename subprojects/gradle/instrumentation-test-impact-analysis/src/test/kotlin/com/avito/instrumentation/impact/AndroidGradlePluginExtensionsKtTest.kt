package com.avito.instrumentation.impact

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.AndroidLibModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.google.common.truth.Correspondence
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class AndroidGradlePluginExtensionsKtTest {

    @Test
    fun test(@TempDir projectDir: File) {
        val testBuildType = "debug"
        val libraryPackageName = "com.avito.some.library"

        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule("app", dependencies = "implementation(project(\":lib\"))"),
                AndroidLibModule(
                    name = "lib",
                    packageName = libraryPackageName,
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
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "app:package${testBuildType.capitalize()}").assertThat().buildSuccessful()

        val appRuntimeSymbolListPath = runtimeSymbolListPath(File(projectDir, "app"), testBuildType)
        val appRuntimeSymbolLines = appRuntimeSymbolListPath.readLines()
        assertThat(appRuntimeSymbolLines).comparingElementsUsing(startWithCorrespondence)
            .containsAtLeast(
                "int id some_new_id",
                "int layout new_layout"
            )

        val libSymbolListWithPackageNamePath = symbolListWithPackageNamePath(File(projectDir, "lib"), testBuildType)
        val libSymbolListLines = libSymbolListWithPackageNamePath.readLines()
        assertThat(libSymbolListLines).containsAtLeast(
            libraryPackageName,
            "id some_new_id",
            "layout new_layout"
        )
    }
}

internal val startWithCorrespondence: Correspondence<String, String> = Correspondence.from({ actual, expected ->
    actual?.startsWith(expected!!) ?: false
}, "string starts with expected prefix")
