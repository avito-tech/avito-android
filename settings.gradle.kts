@file:Suppress("UnstableApiUsage")

rootProject.name = "avito-android-samples"

val useCompositeBuild: String by settings

include(":samples:test-app")
include(":samples:test-app-kaspresso")
if (useCompositeBuild.toBoolean()) {
    include(":samples:test-app-without-backward-compatibility")
}
include(":samples:test-app-core")
include(":samples:test-app-impact:app")

includeBuild("buildscript")

if (useCompositeBuild.toBoolean()) {
    includeBuild("subprojects") {
        dependencySubstitution {
            substitute(module("com.avito.android:instrumentation-tests")).with(project(":gradle:instrumentation-tests"))
            substitute(module("com.avito.android:proxy-toast")).with(project(":android-lib:proxy-toast"))
            substitute(module("com.avito.android:snackbar-proxy")).with(project(":android-lib:snackbar-proxy"))
            substitute(module("com.avito.android:snackbar-rule")).with(project(":android-test:snackbar-rule"))
            substitute(module("com.avito.android:time")).with(project(":common:time"))
            substitute(module("com.avito.android:test-report")).with(project(":android-test:test-report"))
            substitute(module("com.avito.android:junit-utils")).with(project(":common:junit-utils"))
            substitute(module("com.avito.android:toast-rule")).with(project(":android-test:toast-rule"))
            substitute(module("com.avito.android:test-inhouse-runner")).with(project(":android-test:test-inhouse-runner"))
            substitute(module("com.avito.android:test-annotations")).with(project(":common:test-annotations"))
            substitute(module("com.avito.android:ui-testing-core")).with(project(":android-test:ui-testing-core"))
            substitute(module("com.avito.android:report-viewer")).with(project(":common:report-viewer"))
            substitute(module("com.avito.android:file-storage")).with(project(":common:file-storage"))
            substitute(module("com.avito.android:okhttp")).with(project(":common:okhttp"))
        }
    }
}

pluginManagement {

    val artifactoryUrl: String? by settings
    val infraVersion: String by settings
    val kotlinVersion: String by System.getProperties()
    val androidGradlePluginVersion: String by System.getProperties()

    repositories {
        exclusiveContent {
            forRepository {
                maven {
                    setUrl("https://kotlin.bintray.com/kotlinx")
                }
            }
            filter {
                includeGroup("org.jetbrains.kotlinx")
            }
        }
        exclusiveContent {
            forRepository {
                mavenLocal()
            }
            forRepository {
                jcenter()
            }
            forRepository {
                maven {
                    name = "Avito bintray"
                    setUrl("https://dl.bintray.com/avito/maven")
                }
            }
            if (!artifactoryUrl.isNullOrBlank()) {
                forRepository {
                    maven {
                        name = "Local Artifactory"
                        setUrl("$artifactoryUrl/libs-release-local")
                    }
                }
            }
            filter {
                includeModuleByRegex("com\\.avito\\.android", ".*")
            }
        }
        // todo remove after 2020.13
        if (!artifactoryUrl.isNullOrBlank()) {
            exclusiveContent {
                forRepository {
                    maven {
                        name = "JCenter Proxy"
                        setUrl("$artifactoryUrl/jcenter")
                    }
                }
                filter {
                    includeVersion("org.jetbrains.kotlinx", "kotlinx-html-jvm", "0.6.9")
                }
            }
        }
        exclusiveContent {
            forRepository {
                gradlePluginPortal()
            }
            filter {
                includeGroup("com.gradle")
                includeGroup("org.jetbrains.kotlin.jvm")
                includeGroup("com.jfrog.bintray")
                includeGroup("com.slack.keeper")
            }
        }
        exclusiveContent {
            forRepository {
                google()
            }
            filter {
                includeGroupByRegex("com\\.android\\.tools\\.build\\.*")
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

    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            when {
                pluginId.startsWith("com.android.") ->
                    useModule("com.android.tools.build:gradle:$androidGradlePluginVersion")

                pluginId.startsWith("org.jetbrains.kotlin.") ->
                    useVersion(kotlinVersion)

                pluginId.startsWith("com.avito.android") ->
                    useModule("com.avito.android:${pluginId.removePrefix("com.avito.android.")}:$infraVersion")

                pluginId == "com.slack.keeper" ->
                    useModule("com.slack.keeper:keeper:0.6.0")
            }
        }
    }
}
