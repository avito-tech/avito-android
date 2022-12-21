package com.avito.android.proguard_guard

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import com.avito.utils.ResourcesReader
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CheckMergedConfigurationsIntegrationTest {

    @field:TempDir
    private lateinit var projectDir: File

    @Test
    fun `integration - different configurations - fail`() {
        generateProject(
            appConfigFilesCreator = {
                file(
                    name = APP_RULES_PATH,
                    content = """
                        # -keep class com.avito.security.** { *; }
                        """.trimIndent()
                )
                file(
                    name = LOCKED_CONFIG_PATH,
                    content = ResourcesReader.readText("locked-configuration.pro")
                )
            },
            libConfigFilesCreator = {
                file(
                    name = LIB_RULES_PATH,
                    content = """
                        -keepclasseswithmembers class * {
                            @com.avito.android.jsonrpc.annotations.* <methods>;
                        }
                        -keepnames class ru.avito.messenger.api.entity.* { *; }
                        """.trimIndent()
                )
            }
        )

        val build = gradlew(projectDir, ":app:checkReleaseMergedProguard", expectFailure = true)
        val diffFile = projectDir.resolve("app/build/outputs/proguard_guard/release/diff.txt")

        build.assertThat().buildFailed()
        build.assertThat().tasksShouldNotBeTriggered(":app:minifyReleaseWithR8")
        build.assertThat().tasksShouldBeTriggered(":app:shadowedMinifyReleaseWithR8")
        assertThat(diffFile.readText()).isEqualTo(
            """
               --- -keep class com.avito.security.** { *; }
               ...
               +++ -keepnames class ru.avito.messenger.api.entity.* { *; }
               
               """.trimIndent(),
        )
    }

    @Test
    fun `integration - same configurations - success`() {
        generateProject(
            appConfigFilesCreator = {
                file(
                    name = APP_RULES_PATH,
                    content = """
                        -keep class com.avito.security.** { *; }
                        """.trimIndent()
                )
                file(
                    name = LOCKED_CONFIG_PATH,
                    content = ResourcesReader.readText("locked-configuration.pro")
                )
            },
            libConfigFilesCreator = {
                file(
                    name = LIB_RULES_PATH,
                    content = """
                        -keepclasseswithmembers class * {
                            @com.avito.android.jsonrpc.annotations.* <methods>;
                        }
                        """.trimIndent()
                )
            }
        )

        val build = gradlew(projectDir, ":app:checkReleaseMergedProguard")
        val diffFile = projectDir.resolve("app/build/outputs/proguard_guard/release/diff.txt")

        build.assertThat().buildSuccessful()
        build.assertThat().tasksShouldNotBeTriggered(":app:minifyReleaseWithR8")
        build.assertThat().tasksShouldBeTriggered(":app:shadowedMinifyReleaseWithR8")
        assertThat(diffFile.exists()).isFalse()
    }

    @Test
    fun `integration - no shadow task - original task called`() {
        generateProject(
            appConfigFilesCreator = {
                file(
                    name = APP_RULES_PATH,
                    content = """
                        -keep class com.avito.security.** { *; }
                        """.trimIndent()
                )
                file(
                    name = LOCKED_CONFIG_PATH,
                    content = ResourcesReader.readText("locked-configuration.pro")
                )
            },
            libConfigFilesCreator = {
                file(
                    name = LIB_RULES_PATH,
                    content = """
                        -keepclasseswithmembers class * {
                            @com.avito.android.jsonrpc.annotations.* <methods>;
                        }
                        """.trimIndent()
                )
            },
            appBuildGradleExtra = """
                android {
                    buildTypes {
                        release {
                            minifyEnabled true
                            proguardFile getDefaultProguardFile('proguard-android-optimize.txt')
                            proguardFile '$APP_RULES_PATH'
                        }
                    }
                }
                proguardGuard {
                    lockVariant("release") {
                        shadowR8Task = false
                        lockedConfigurationFile = file("$LOCKED_CONFIG_PATH")
                    }
                }
                """.trimIndent()
        )

        val build = gradlew(projectDir, ":app:checkReleaseMergedProguard")
        val diffFile = projectDir.resolve("app/build/outputs/proguard_guard/release/diff.txt")

        build.assertThat().buildSuccessful()
        build.assertThat().tasksShouldBeTriggered(":app:minifyReleaseWithR8")
        build.assertThat().tasksShouldNotBeTriggered(":app:shadowedMinifyReleaseWithR8")
        assertThat(diffFile.exists()).isFalse()
    }

    private fun generateProject(
        appConfigFilesCreator: File.(AndroidAppModule) -> Unit,
        libConfigFilesCreator: File.() -> Unit,
        appBuildGradleExtra: String? = null,
    ) {
        val libraryModule = "lib"

        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.proguard-guard")
                    },
                    buildGradleExtra = appBuildGradleExtra ?: """
                        android {
                            buildTypes {
                                release {
                                    minifyEnabled true
                                    proguardFile getDefaultProguardFile('proguard-android-optimize.txt')
                                    proguardFile '$APP_RULES_PATH'
                                }
                            }
                        }
                        proguardGuard {
                            lockVariant("release") {
                                lockedConfigurationFile = file("$LOCKED_CONFIG_PATH")
                            }
                        }
                        """.trimIndent(),
                    mutator = appConfigFilesCreator,
                    dependencies = setOf(project(":$libraryModule"))
                ),
                AndroidLibModule(
                    name = libraryModule,
                    enableKotlinAndroidPlugin = false,
                    buildGradleExtra = """
                        android {
                            buildTypes {
                                release {
                                    minifyEnabled true
                                    consumerProguardFiles '$LIB_RULES_PATH'
                                }
                            }
                        }
                        """.trimIndent(),
                    mutator = libConfigFilesCreator,
                )
            ),
        ).generateIn(projectDir)
    }

    companion object {
        const val LOCKED_CONFIG_PATH = "proguard-guard/release/guarded-configuration.pro"
        const val APP_RULES_PATH = "proguard-rules.pro"
        const val LIB_RULES_PATH = "lib-proguard-rules.pro"
    }
}
