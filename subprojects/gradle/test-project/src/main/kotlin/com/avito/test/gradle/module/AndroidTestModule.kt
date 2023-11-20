package com.avito.test.gradle.module

import com.avito.test.gradle.buildToolsVersion
import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.dir
import com.avito.test.gradle.files.InstrumentationTest
import com.avito.test.gradle.files.androidManifest
import com.avito.test.gradle.files.build_gradle
import com.avito.test.gradle.files.build_gradle_kts
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.kotlinVersion
import com.avito.test.gradle.minSdkVersion
import com.avito.test.gradle.module
import com.avito.test.gradle.plugin.PluginsSpec
import com.avito.test.gradle.sdkVersion
import java.io.File

/**
 * Module for `com.android.test` plugin
 */
public class AndroidTestModule(
    override val name: String,
    override val packageName: String = "com.$name",
    override val imports: List<String> = emptyList(),
    override val plugins: PluginsSpec = PluginsSpec(),
    override val buildGradleExtra: String = "",
    override val modules: List<Module> = emptyList(),
    override val enableKotlinAndroidPlugin: Boolean = true,
    override val dependencies: Set<GradleDependency> = emptySet(),
    override val useKts: Boolean = false,

    private val targetApplicationModuleName: String,
    private val testBuildType: String = "benchmark",
    private val instrumentationTests: List<InstrumentationTest> = emptyList(),
    private val mutator: File.(AndroidTestModule) -> Unit = {}
) : AndroidModule {

    override fun generateIn(file: File) {
        file.module(name) {
            dir("src") {
                dir("main") {
                    androidManifest(packageName = packageName)
                    if (enableKotlinAndroidPlugin) {
                        dir("kotlin") {
                            kotlinClass("SomeClass", packageName)
                            instrumentationTests.forEach { it.generateIn(this) }
                        }
                    }
                }
            }

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
            this.mutator(this@AndroidTestModule)
        }
    }

    private fun dependencies(): String {
        return """
            |dependencies {
            |    ${dependencies.joinToString(separator = "\n\t", transform = { it.getScriptRepresentation() })}
            |    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
            |    implementation("junit:junit:4.13")
            |    implementation("androidx.test.ext:junit-ktx:1.1.5")
            |    implementation("androidx.benchmark:benchmark-macro-junit4:1.2.0-alpha14")
            |}
            |
            """.trimMargin()
    }

    private fun androidExtension(useKts: Boolean): String {
        return if (useKts) {
            """
            |android {
            |   namespace = "$packageName"
            |
            |   compileSdkVersion($sdkVersion)
            |   buildToolsVersion = "$buildToolsVersion"
            |   defaultConfig {
            |       minSdk($minSdkVersion)
            |   }
            |   buildTypes {
            |       register("$testBuildType") {
            |           debuggable = true
            |           signingConfig = debug.signingConfig
            |           matchingFallbacks += ["release"]
            |       }
            |   }
            |   
            |   targetProjectPath = ":$targetApplicationModuleName"
            |}
            """.trimMargin()
        } else {
            """
            |android {
            |   namespace = "$packageName"
            |
            |   compileSdkVersion $sdkVersion
            |   buildToolsVersion "$buildToolsVersion"
            |   defaultConfig {
            |       minSdk $minSdkVersion
            |   }
            |   buildTypes {
            |       $testBuildType {
            |           debuggable = true
            |           signingConfig debug.signingConfig
            |           matchingFallbacks = ["release"]
            |       }
            |   }
            |   
            |   targetProjectPath = ":$targetApplicationModuleName"
            |}
            """.trimMargin()
        }
    }

    private fun plugins(): PluginsSpec =
        com.avito.test.gradle.plugin.plugins {
            id("com.android.test")
            id("org.jetbrains.kotlin.android")
            if (enableKotlinAndroidPlugin) { id("kotlin-android") }
        }
            .plus(plugins)
}
