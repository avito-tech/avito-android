package com.avito.test.gradle.module

import com.avito.test.gradle.buildToolsVersion
import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.files.androidManifest
import com.avito.test.gradle.files.build_gradle
import com.avito.test.gradle.files.build_gradle_kts
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.kotlinVersion
import com.avito.test.gradle.module
import com.avito.test.gradle.plugin.PluginsSpec
import com.avito.test.gradle.plugin.plugins
import com.avito.test.gradle.sdkVersion
import com.avito.test.gradle.targetSdk
import java.io.File

public class AndroidLibModule(
    override val name: String,
    override val packageName: String = "com.$name",
    imports: List<String> = emptyList(),
    override val plugins: PluginsSpec = PluginsSpec(),
    override val buildGradleExtra: String = "",
    override val modules: List<Module> = emptyList(),
    override val enableKotlinAndroidPlugin: Boolean = true,
    override val dependencies: Set<GradleDependency> = emptySet(),
    override val useKts: Boolean = false,
    private val mutator: File.() -> Unit = {},
) : AndroidModule {

    override val buildFileImports: List<String>

    init {
        val kotlinImports = if (enableKotlinAndroidPlugin) {
            listOf("import org.jetbrains.kotlin.gradle.tasks.KotlinCompile")
        } else {
            emptyList()
        }
        buildFileImports = imports + kotlinImports
    }

    /**
     * Don't use `android.resourcePrefix` because clients won't be able to add resource without it.
     */
    private val resourcesPrefix: String = this.name.replace("[^a-z0-9_]+".toRegex(), "_")

    override fun generateIn(file: File) {
        file.module(name) {

            val kotlinPluginExtra = if (enableKotlinAndroidPlugin) {
                """
                |sourceSets {
                |   getByName("main") {
                |       java.srcDirs(file("src/main/kotlin"))
                |   }
                |}
                """.trimMargin()
            } else {
                ""
            }

            val buildGradleContent = """
                |${imports()}
                |${plugins()}
                |
                |$buildGradleExtra
                |
                |${kotlinExtension(useKts)}
                |
                |android {
                |   namespace = "$packageName"
                |   compileSdkVersion($sdkVersion)
                |   buildToolsVersion("$buildToolsVersion")
                |   
                |   defaultConfig {
                |       targetSdk = $targetSdk
                |   }
                |   
                |   $kotlinPluginExtra
                |}
                |
                |dependencies {
                |   ${dependencies.joinToString(separator = "\n\t", transform = { it.getScriptRepresentation() })}
                |   implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
                |}
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

            dir("src/main") {
                androidManifest()
                if (enableKotlinAndroidPlugin) {
                    dir("kotlin") {
                        kotlinClass("SomeClass", packageName)
                    }
                }
                dir("res") {
                    dir("layout") {
                        file(
                            name = "${resourcesPrefix}_lib.xml",
                            content = """|<?xml version="1.0" encoding="utf-8"?>
                                         |<TextView xmlns:android="http://schemas.android.com/apk/res/android"
                                         |   xmlns:tools="http://schemas.android.com/tools"
                                         |      android:id="@+id/title"
                                         |      android:layout_width="match_parent"
                                         |      android:layout_height="wrap_content"
                                         |      android:duplicateParentState="true"
                                         |      tools:text="Title" />
                                      """.trimMargin()
                        )
                    }
                }
            }
            this.mutator()
        }
    }

    private fun kotlinExtension(useKts: Boolean): String {
        @Suppress("VariableNaming")
        val setKotlinTarget_1_8 = if (enableKotlinAndroidPlugin) {
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

    private fun plugins(): PluginsSpec =
        plugins {
            id("com.android.library")
            if (enableKotlinAndroidPlugin) id("kotlin-android")
        }.plus(plugins)
}
