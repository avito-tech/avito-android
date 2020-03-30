package com.avito.bytecode

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.kotlinClass
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.junit.Test
import java.io.File

class TestApp {

    fun createTestProject(projectDir: File) {

        val packageName = "com.test.app"
        TestProjectGenerator(
            plugins = listOf("com.avito.android.impact"),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    packageName = packageName,
                    versionCode = "12",
                    versionName = "12.0",
                    plugins = listOf("kotlin-android"),
                    dependencies = """
                        androidTestImplementation("junit:junit:4.13")
                    """.trimIndent()
                ).apply {
                    androidTest {
                        kotlinClass(createSimpleScreen(packageName))
                        kotlinClass(createSimpleTest())
                    }
                    resources {
                        dir("layout") {
                            file(
                                "page1.xml", """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/page1_root_id" />
                            """.trimIndent()
                            )
                        }
                    }
                }
            )
        )
            .generateIn(projectDir)

        gradlew(projectDir, ":app:analyzeTestBytecode")
            .assertThat()
            .buildSuccessful()
    }

    private fun createSimpleScreen(packageName: String): FileSpec {
        return FileSpec.builder("com.test.screen", "Page1")
            .addType(
                TypeSpec.classBuilder("Page1")
                    .addProperty(
                        PropertySpec.builder("rootId", Int::class)
                            .initializer("$packageName.R.id.page1_root_id")
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun createSimpleTest(): FileSpec {
        return FileSpec.builder("com.test", "CommonTest")
            .addType(
                TypeSpec.classBuilder("CommonTest")
                    .addFunction(
                        FunSpec.builder("test")
                            .addAnnotation(Test::class)
                            .build()
                    )
                    .build()
            )
            .build()
    }
}
