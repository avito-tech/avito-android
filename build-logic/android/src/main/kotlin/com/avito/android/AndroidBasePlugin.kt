package com.avito.android

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.tasks.VerifyLibraryResourcesTask
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidBasePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            extensions.configure(BaseExtension::class.java) { androidBase ->
                with(androidBase) {
                    sourceSets { sourceSets ->
                        sourceSets.named("main").configure { it.java.srcDir("src/main/kotlin") }
                        sourceSets.named("androidTest").configure { it.java.srcDir("src/androidTest/kotlin") }
                        sourceSets.named("test").configure { it.java.srcDir("src/test/kotlin") }
                    }

                    buildToolsVersion(libs.versions.buildTools.get())
                    compileSdkVersion(libs.versions.compileSdk.get().toInt())

                    defaultConfig {
                        it.minSdk = libs.versions.minSdk.get().toInt()
                        it.targetSdk = libs.versions.targetSdk.get().toInt()
                    }

                    compileOptions {
                        it.sourceCompatibility = JavaVersion.VERSION_1_8
                        it.targetCompatibility = JavaVersion.VERSION_1_8
                    }

                    lintOptions { lint ->
                        with(lint) {
                            isAbortOnError = false
                            isWarningsAsErrors = true
                            textReport = true
                            isQuiet = true
                            isCheckReleaseBuilds = false
                        }
                    }

                    @Suppress("UnstableApiUsage")
                    with(buildFeatures) {
                        aidl = false
                        compose = false
                        buildConfig = false
                        prefab = false
                        renderScript = false
                        resValues = false
                        shaders = false
                        viewBinding = false
                    }
                }
            }

            tasks.withType(VerifyLibraryResourcesTask::class.java).configureEach {
                // todo fix and enable MBS-11914
                it.onlyIf { false }
            }
        }
    }
}
