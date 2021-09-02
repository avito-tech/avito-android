package com.avito.ci

import com.avito.android.plugin.artifactory.artifactoryPasswordParameterName
import com.avito.android.plugin.artifactory.artifactoryUserParameterName
import com.avito.instrumentation.instrumentationPluginId
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import java.io.File

internal abstract class BaseCiStepsPluginTest {
    protected lateinit var projectDir: File

    @Suppress("MaxLineLength")
    protected fun generateProjectWithConfiguredCiSteps(
        uploadCrashlyticsMappingFileEnabled: Boolean = true
    ) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.impact")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "appA",
                    dependencies = setOf(GradleDependency.Safe.project(":shared")),
                    plugins = plugins {
                        applyWithBuildscript(
                            buildscriptClasspath = "com.google.gms:google-services:4.3.3",
                            pluginId = "com.google.gms.google-services"
                        )
                        applyWithBuildscript(
                            buildscriptClasspath = "com.google.firebase:firebase-crashlytics-gradle:2.7.1",
                            pluginId = "com.google.firebase.crashlytics"
                        )

                        id("com.avito.android.signer")
                        id(instrumentationPluginId)
                        id("com.avito.android.prosector")
                        id("com.avito.android.qapps")
                        id("com.avito.android.artifactory-app-backup")
                        id("com.avito.android.cd")
                        id("maven-publish")
                    },
                    imports = listOf(
                        "import com.avito.cd.BuildVariant"
                    ),
                    buildGradleExtra = """
                            ${registerUiTestConfigurations("regress", "pr")}
                            signService {
                                url.set("https://signer/")
                                apk(android.buildTypes.release, "no_matter")
                                bundle(android.buildTypes.release, "no_matter")
                            }
                            prosector {
                                host("https://prosector")
                            }
                            qapps {
                                serviceUrl.set("https://qapps")
                                branchName.set("develop")
                                comment.set("build#1")
                            }
                            android {
                                buildTypes {
                                    release {
                                        minifyEnabled true
                                        proguardFile("proguard.pro")
                                        firebaseCrashlytics.mappingFileUploadEnabled = true
                                    }
                                }
                            }
                            builds {
                                release {

                                    uiTests {
                                        configurations "regress"
                                    }
                                    unitTests { }
                                    lint { }

                                    artifacts {
                                        apk("debugApk", BuildVariant.DEBUG, "com.appA", "${'$'}{project.buildDir}/outputs/apk/debug/appA-debug.apk") {}
                                        apk("releaseApk", BuildVariant.RELEASE, "com.appA", "${'$'}{project.buildDir}/outputs/apk/release/appA-release.apk") {
                                            signature = "1221e21e21e"
                                        }
                                        bundle("releaseBundle", BuildVariant.RELEASE, "com.appA", "${'$'}{project.buildDir}/outputs/release/debug/appA-release.aab") {
                                            signature = "12e23rrr34r"
                                        }
                                        mapping("releaseMapping", BuildVariant.RELEASE, "${'$'}{project.buildDir}/reports/mapping.txt")
                                        file("nonExistedJson","${'$'}{project.buildDir}/reports/not-existed-file.json")
                                    }

                                    uploadToQapps {
                                        artifacts = ['releaseApk']
                                    }
                                    uploadToProsector {
                                        artifacts = ['debugApk']
                                    }
                                    uploadToArtifactory {
                                        artifacts = ['debugApk', 'releaseApk']
                                    }
                                    uploadBuildResult {
                                        uiTestConfiguration = "regress"
                                    }
                                    deploy {
                                        uploadCrashlyticsProguardMappingFile = $uploadCrashlyticsMappingFileEnabled
                                    }
                                }
                            }
                        """.trimIndent()
                ),
                AndroidAppModule(
                    name = "appB",
                    dependencies = setOf(GradleDependency.Safe.project(":shared")),
                    plugins = plugins {
                        id("com.avito.android.cd")
                    }
                ),
                AndroidLibModule(
                    name = "shared",
                    dependencies = setOf(GradleDependency.Safe.project(":transitive"))
                ),
                AndroidLibModule(
                    name = "transitive"
                ),
                AndroidLibModule(
                    name = "independent"
                )
            )
        ).generateIn(projectDir)
    }

    protected fun runTask(
        vararg args: String,
        dryRun: Boolean = true,
        expectedFailure: Boolean = false
    ) = runTask(
        projectDir,
        *args,
        "-PartifactoryUrl=http://artifactory",
        "-P$artifactoryUserParameterName=user",
        "-P$artifactoryPasswordParameterName=password",
        dryRun = dryRun,
        expectedFailure = expectedFailure
    )
}
