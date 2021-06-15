package com.avito.test.gradle.module

import com.avito.test.gradle.buildToolsVersion
import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.files.InstrumentationTest
import com.avito.test.gradle.files.androidManifest
import com.avito.test.gradle.files.build_gradle
import com.avito.test.gradle.files.build_gradle_kts
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.kotlinVersion
import com.avito.test.gradle.module
import com.avito.test.gradle.plugin.PluginsSpec
import com.avito.test.gradle.plugin.plugins
import com.avito.test.gradle.sdkVersion
import java.io.File

class AndroidAppModule(
    override val name: String,
    override val packageName: String = "com.$name",
    override val plugins: PluginsSpec = PluginsSpec(),
    override val buildGradleExtra: String = "",
    override val modules: List<Module> = emptyList(),
    override val enableKotlinAndroidPlugin: Boolean = true,
    override val dependencies: Set<GradleDependency> = emptySet(),
    override val useKts: Boolean = false,
    private val enableKapt: Boolean = false,
    private val instrumentationTests: List<InstrumentationTest> = emptyList(),
    private val versionName: String = "",
    private val versionCode: Int = 1,
    private val customScript: String = "",
    private val imports: List<String> = emptyList(),
    private val mutator: File.(AndroidAppModule) -> Unit = {}
) : AndroidModule {

    override fun generateIn(file: File) {
        file.module(name) {
            dir("src") {
                dir("main") {
                    androidManifest(packageName = packageName)
                    if (enableKotlinAndroidPlugin || enableKapt) {
                        dir("kotlin") {
                            kotlinClass("SomeClass", packageName)
                        }
                    }
                    dir("res") {
                        dir("values") {
                            file(
                                name = "id.xml",
                                content = """<?xml version="1.0" encoding="utf-8"?>
                                <resources>
                                    <item name="some_id" type="id" />
                                </resources>
                            """.trimIndent()
                            )
                        }
                    }
                }
                dir("androidTest") {
                    if (enableKotlinAndroidPlugin || enableKapt) {
                        dir("kotlin") {
                            kotlinClass("SomeTestClass", packageName)
                            instrumentationTests.forEach { it.generateIn(this) }
                        }
                    }
                }
            }

            file(
                name = "proguard.pro",
                content = """
                -ignorewarnings
                -keep public class * {
                    public protected *;
                }
                """.trimIndent()
            )

            val buildGradleContent = """
                |${imports()}
                |${plugins()}
                |
                |$buildGradleExtra
                |
                |${androidExtension(useKts)}
                |
                |${dependencies()}
                |
                |$customScript
                """.trimMargin()

            if (useKts) {
                build_gradle_kts {
                    writeText(buildGradleContent)
                }
            } else {
                build_gradle {
                    writeText(buildGradleContent)
                }
            }
            this.mutator(this@AndroidAppModule)
        }
    }

    private fun dependencies(): String {
        return """dependencies {
    ${dependencies.joinToString(separator = "\n\t", transform = { it.getScriptRepresentation() })}
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    androidTestImplementation("junit:junit:4.13")
    testImplementation("junit:junit:4.13")
    ${
            if (enableKapt) {
                """
                            implementation("com.google.dagger:dagger:2.29.1")
                            kapt("com.google.dagger:dagger-compiler:2.29.1")
                            """.trimIndent()
            } else {
                ""
            }
        }
}"""
    }

    private fun androidExtension(useKts: Boolean): String {
        val enableKotlinAndroid = if (enableKotlinAndroidPlugin || enableKapt) {
            """
            |sourceSets {
            |   getByName("main") {
            |       java.srcDirs(file("src/main/kotlin"))
            |   }
            |   getByName("test") {
            |       java.srcDirs(file("src/test/kotlin"))
            |   }
            |   getByName("androidTest") {
            |       java.srcDirs(file("src/androidTest/kotlin"))
            |   }
            |}
            """.trimMargin()
        } else {
            ""
        }

        val disableLintVitalRelease = """
            |afterEvaluate{
            |       tasks.named("lintVitalRelease").configure { onlyIf { false } }
            |}
            """.trimMargin()

        return if (useKts) {
            """
            |android {
            |   compileSdkVersion($sdkVersion)
            |   buildToolsVersion = "$buildToolsVersion"
            |   defaultConfig {
            |       applicationId = "$packageName"
            |       versionCode = $versionCode
            |       versionName = "$versionName"
            |   }
            |   buildTypes {
            |       val debug = getByName("debug")
            |       register("staging") {
            |           initWith(debug)
            |           matchingFallbacks += listOf("debug")
            |       }
            |   }
            |   $enableKotlinAndroid
            |   $disableLintVitalRelease
            |}
            """.trimMargin()
        } else {
            """
            |android {
            |   compileSdkVersion $sdkVersion
            |   buildToolsVersion "$buildToolsVersion"
            |   defaultConfig {
            |       applicationId "$packageName"
            |       versionCode $versionCode
            |       versionName "$versionName"
            |   }
            |   buildTypes {
            |       release {}
            |       debug {}
            |       staging {
            |           initWith(debug)
            |           matchingFallbacks = ["debug"]
            |       }
            |   }
            |   $enableKotlinAndroid
            |   $disableLintVitalRelease
            |}
            """.trimMargin()
        }
    }

    private fun plugins(): PluginsSpec =
        plugins {
            id("com.android.application")
            if (enableKotlinAndroidPlugin || enableKapt) id("kotlin-android")
            if (enableKapt) id("kotlin-kapt")
        }
            .plus(plugins)

    private fun imports() = imports.joinToString(separator = "\n")
}
