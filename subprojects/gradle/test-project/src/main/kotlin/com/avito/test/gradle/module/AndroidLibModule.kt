package com.avito.test.gradle.module

import com.avito.test.gradle.buildToolsVersion
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.files.androidManifest
import com.avito.test.gradle.files.build_gradle
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.kotlinVersion
import com.avito.test.gradle.module
import com.avito.test.gradle.sdkVersion
import java.io.File

class AndroidLibModule(
    override val name: String,
    override val packageName: String = "com.$name",
    override val plugins: List<String> = emptyList(),
    override val buildGradleExtra: String = "",
    override val modules: List<Module> = emptyList(),
    override val enableKotlinAndroidPlugin: Boolean = true,
    private val dependencies: String = "",
    private val mutator: File.() -> Unit = {}
) : AndroidModule {

    override fun generateIn(file: File) {
        file.module(name) {

            build_gradle {
                writeText(
                    """
plugins {
    id 'com.android.library'
    ${if (enableKotlinAndroidPlugin) "id 'kotlin-android'" else ""}
    ${plugins.joinToString(separator = "\n") { "id '$it'" }}
}

$buildGradleExtra

android {
    compileSdkVersion $sdkVersion
    buildToolsVersion "$buildToolsVersion"
    ${
                        if (enableKotlinAndroidPlugin) """
    sourceSets {
        main {
            java.srcDir("src/main/kotlin")
        }
    }
    """.trimIndent() else ""
                    }
}

dependencies {
    $dependencies
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
    """.trimIndent()
                )
            }
            dir("src/main") {
                androidManifest(packageName = packageName)
                if (enableKotlinAndroidPlugin) {
                    dir("kotlin") {
                        kotlinClass("SomeClass", packageName)
                    }
                }
                dir("res") {
                    dir("layout") {
                        file(
                            "lib.xml", content = """<?xml version="1.0" encoding="utf-8"?>
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
        android:id="@+id/title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:duplicateParentState="true"
        tools:text="Title" />
                            """.trimIndent()
                        )
                    }
                }
            }
            this.mutator()
        }
    }
}
