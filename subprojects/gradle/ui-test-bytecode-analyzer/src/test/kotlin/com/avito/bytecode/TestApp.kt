package com.avito.bytecode

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
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
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    versionCode = "12",
                    versionName = "12.0",
                    plugins = listOf("kotlin-android")
                ).apply {
                    androidTest {
                        kotlinClass(createSimpleScreen())
                        kotlinClass(createSimpleTest())
                    }
                }
            )
        )
            .generateIn(projectDir)

        gradlew(projectDir, ":app:assembleAndroidTest", "--info", isPlugin = false)
            .assertThat()
            .buildSuccessful()
    }

    private fun createSimpleScreen(): FileSpec {
        return FileSpec.builder("com.test.screen", "Page1")
            .addType(
                TypeSpec.classBuilder("Page1")
                    .addProperty(
                        PropertySpec.builder("rootId", Int::class)
                            .initializer("R.id.page1_root_id")
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
