package com.avito.test.gradle.module

import com.avito.test.gradle.buildToolsVersion
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.files.InstrumentationTest
import com.avito.test.gradle.files.androidManifest
import com.avito.test.gradle.files.build_gradle
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.kotlinVersion
import com.avito.test.gradle.module
import com.avito.test.gradle.sdkVersion
import java.io.File

class AndroidAppModule(
    override val name: String,
    override val packageName: String = "com.$name",
    override val plugins: List<String> = emptyList(),
    override val buildGradleExtra: String = "",
    override val modules: List<Module> = emptyList(),
    override val enableKotlinAndroidPlugin: Boolean = true,
    private val enableKapt: Boolean = false,
    private val instrumentationTests: List<InstrumentationTest> = emptyList(),
    private val dependencies: String = "",
    private val versionName: String = "",
    private val versionCode: String = "",
    private val customScript: String = "",
    private val imports: List<String> = emptyList(),
    private val mutator: File.(AndroidAppModule) -> Unit = {}
) : AndroidModule {

    override fun generateIn(file: File) {
        file.module(name) {
            dir("src") {
                dir("main") {
                    androidManifest(packageName = packageName)
                    if(enableKotlinAndroidPlugin || enableKapt) {
                        dir("kotlin") {
                            kotlinClass("SomeClass", packageName)
                        }
                    }
                    dir("res") {
                        dir("values") {
                            file(
                                "id.xml", content = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <item name="some_id" type="id" />
</resources>
                            """.trimIndent()
                            )
                        }
                    }
                }
                dir("androidTest") {
                    if(enableKotlinAndroidPlugin || enableKapt) {
                        dir("kotlin") {
                            kotlinClass("SomeTestClass", packageName)
                            instrumentationTests.forEach { it.generateIn(this) }
                        }
                    }
                }
            }

            file(
                "proguard.pro", """
-ignorewarnings
-keep public class * {
    public protected *;
}
""".trimIndent()
            )

            build_gradle {
                writeText(
                    """${imports.joinToString(separator = "\n")}


plugins {
    id 'com.android.application'
    ${if(enableKotlinAndroidPlugin || enableKapt) "id 'kotlin-android'" else ""}
    ${if(enableKapt) "id 'kotlin-kapt'" else ""}
${plugins.joinToString(separator = "\n") { "    id '$it'" }}
}

$buildGradleExtra

android {
    compileSdkVersion $sdkVersion
    buildToolsVersion "$buildToolsVersion"
    defaultConfig {
        applicationId "$packageName"
        versionCode $versionCode
        versionName "$versionName"
    }
    buildTypes {
        release {}
        debug {}
        staging {
            initWith(debug)
            matchingFallbacks = ["debug"]
        }
    }
    ${if(enableKotlinAndroidPlugin || enableKapt) """
        sourceSets {
        main {
            java.srcDir("src/main/kotlin")
        }
        test {
            java.srcDir("src/test/kotlin")
        }
        androidTest {
            java.srcDir("src/androidTest/kotlin")
        }
    }
    """.trimIndent() else ""}
}

afterEvaluate{
    tasks.named("lintVitalRelease").configure { onlyIf { false } }
}

dependencies {
    $dependencies
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    androidTestImplementation("junit:junit:4.13")
    testImplementation("junit:junit:4.13")
    ${if(enableKapt) """
    implementation("com.google.dagger:dagger:2.29.1")
    kapt("com.google.dagger:dagger-compiler:2.29.1")
    """.trimIndent() else ""}
}

$customScript
""".trimIndent()
                )
            }
            this.mutator(this@AndroidAppModule)
        }
    }
}
