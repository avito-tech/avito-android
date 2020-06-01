@file:Suppress("UnstableApiUsage")

buildscript {
    buildscript {
        dependencies {
            classpath("com.avito.android:buildscript")
        }
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") apply false
    id("com.android.application") apply false
}

/**
 * We use exact version to provide consistent environment and avoid build cache issues
 * (AGP tasks has artifacts from build tools)
 */
val buildTools = "29.0.2"
val javaVersion = JavaVersion.VERSION_1_8
val compileSdk = 29

subprojects {

    repositories {
        jcenter()
        exclusiveContent {
            forRepository {
                maven {
                    setUrl("https://kotlin.bintray.com/kotlinx")
                }
            }
            filter {
                includeModuleByRegex("org\\.jetbrains\\.kotlinx", "kotlinx-cli.*")
            }
        }
        exclusiveContent {
            forRepository {
                google()
            }
            forRepository {
                mavenCentral()
            }
            filter {
                includeModuleByRegex("com\\.android.*", "(?!r8).*")
                includeModuleByRegex("com\\.google\\.android.*", ".*")
                includeGroupByRegex("androidx\\..*")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    name = "R8 releases"
                    setUrl("http://storage.googleapis.com/r8-releases/raw")
                }
            }
            filter {
                includeModule("com.android.tools", "r8")
            }
        }
    }

    plugins.matching { it is com.android.build.gradle.AppPlugin || it is com.android.build.gradle.LibraryPlugin }.whenPluginAdded {
        configure<com.android.build.gradle.BaseExtension> {
            sourceSets {
                named("main").configure { java.srcDir("src/main/kotlin") }
                named("androidTest").configure { java.srcDir("src/androidTest/kotlin") }
                named("test").configure { java.srcDir("src/test/kotlin") }
            }

            buildToolsVersion(buildTools)
            compileSdkVersion(compileSdk)

            compileOptions {
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }

            defaultConfig {
                minSdkVersion(21)
                targetSdkVersion(28)
            }

            lintOptions {
                isAbortOnError = false
                isWarningsAsErrors = true
                textReport = true
            }
        }
    }

    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper> {
        this@subprojects.run {
            tasks {
                withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
                    kotlinOptions {
                        jvmTarget = javaVersion.toString()
                        allWarningsAsErrors = false //todo we use deprecation a lot, and it's a compiler warning
                    }
                }
            }

            dependencies {
                "implementation"(Dependencies.kotlinStdlib)
            }
        }
    }
}