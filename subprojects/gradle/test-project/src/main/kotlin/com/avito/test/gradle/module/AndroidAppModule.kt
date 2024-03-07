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

public class AndroidAppModule(
    override val name: String,
    override val packageName: String = "com.$name",
    imports: List<String> = emptyList(),
    override val plugins: PluginsSpec = PluginsSpec(),
    override val buildGradleExtra: String = "",
    override val modules: List<Module> = emptyList(),
    override val enableKotlinAndroidPlugin: Boolean = true,
    override val dependencies: Set<GradleDependency> = emptySet(),
    override val useKts: Boolean = false,
    private val enableKapt: Boolean = false,
    private val enableKsp: Boolean = false,
    private val instrumentationTests: List<InstrumentationTest> = emptyList(),
    private val versionName: String = "",
    private val versionCode: Int = 1,
    private val buildTypeName: String = "staging",
    private val mutator: File.(AndroidAppModule) -> Unit = {}
) : AndroidModule {

    override val buildFileImports: List<String>

    init {
        val kotlinImports = if (enableKotlinAndroidPlugin || enableKapt) {
            listOf("import org.jetbrains.kotlin.gradle.tasks.KotlinCompile")
        } else {
            emptyList()
        }
        buildFileImports = imports + kotlinImports
    }

    override fun generateIn(file: File) {
        file.module(name) {
            dir("src") {
                dir("main") {
                    androidManifest()
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
                |${kotlinExtension(useKts)}
                |
                |${androidExtension(useKts)}
                |
                |${dependencies()}
                |
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
        ${
            if (enableKsp) {
                """
                 ksp("com.airbnb.android:paris-processor:2.0.0")
             """.trimIndent()
            } else {
                ""
            }
        }
    }"""
    }

    private fun kotlinExtension(useKts: Boolean): String {
        @Suppress("VariableNaming")
        val setKotlinTarget_1_8 = if (enableKotlinAndroidPlugin || enableKapt) {
            if (useKts) {
                """
                |tasks.withType(KotlinCompile::class.java).configureEach {
                |   kotlinOptions {
                |       jvmTarget = JavaVersion.VERSION_1_8.toString()
                |    }
                |}""".trimMargin()
            } else {
                """
                |tasks.withType(KotlinCompile).configureEach {
                |   kotlinOptions {
                |       jvmTarget = JavaVersion.VERSION_1_8.toString()
                |    }
                |}""".trimMargin()
            }
        } else {
            ""
        }
        return setKotlinTarget_1_8
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

        val disableLintVitalRelease = if (useKts) {
            """
            |lintOptions {
            |   isCheckReleaseBuilds = false
            |}
            """.trimMargin()
        } else {
            """
            |lintOptions {
            |   checkReleaseBuilds = false
            |}
            """.trimMargin()
        }

        val namespace = "namespace = \"$packageName\""

        return if (useKts) {
            """
            |android {
            |   $namespace
            |   compileSdkVersion($sdkVersion)
            |   buildToolsVersion = "$buildToolsVersion"
            |   defaultConfig {
            |       applicationId = "$packageName"
            |       versionCode = $versionCode
            |       versionName = "$versionName"
            |   }
            |   buildTypes {
            |       val debug = getByName("debug") {
            |           applicationIdSuffix = ".debug"
            |       }
            |       register("$buildTypeName") {
            |           initWith(debug)
            |           applicationIdSuffix = ".$buildTypeName"
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
            |   $namespace
            |   compileSdkVersion $sdkVersion
            |   buildToolsVersion "$buildToolsVersion"
            |   defaultConfig {
            |       applicationId "$packageName"
            |       versionCode $versionCode
            |       versionName "$versionName"
            |   }
            |   buildTypes {
            |       release {}
            |       debug {
            |           applicationIdSuffix = ".debug"
            |       }
            |       $buildTypeName {
            |           initWith(debug)
            |           applicationIdSuffix = ".$buildTypeName"
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
            if (enableKotlinAndroidPlugin || enableKapt || enableKsp) id("kotlin-android")
            if (enableKapt) id("kotlin-kapt")
            if (enableKsp) id("com.google.devtools.ksp")
        }
            .plus(plugins)
}
