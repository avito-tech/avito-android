@file:Suppress("UnstableApiUsage")

import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.kubernetesCredentials

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
    id("com.avito.android.instrumentation-tests") apply false
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

    plugins.withType<com.avito.instrumentation.InstrumentationTestsPlugin>() {
        extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {

            //todo make these params optional features in plugin
            reportApiUrl = project.getOptionalStringProperty("avito.report.url") ?: "http://stub"
            reportApiFallbackUrl = project.getOptionalStringProperty("avito.report.fallbackUrl") ?: "http://stub"
            reportViewerUrl = project.getOptionalStringProperty("avito.report.viewerUrl") ?: "http://stub"
            registry = project.getOptionalStringProperty("avito.registry", "registry") ?: "registry"
            sentryDsn =
                project.getOptionalStringProperty("avito.instrumentaion.sentry.dsn") ?: "http://stub-project@stub-host/0"
            slackToken = project.getOptionalStringProperty("avito.slack.token") ?: "stub"
            fileStorageUrl = project.getOptionalStringProperty("avito.fileStorage.url") ?: "http://stub"

            logcatTags = setOf(
                "UITestRunner:*",
                "ActivityManager:*",
                "ReportTestListener:*",
                "StorageJsonTransport:*",
                "TestReport:*",
                "VideoCaptureListener:*",
                "TestRunner:*",
                "SystemDialogsManager:*",
                "AndroidJUnitRunner:*",
                "ito.android.de:*", //по этому тэгу система пишет логи об использовании hidden/restricted api https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces
                "*:E"
            )

            instrumentationParams = mapOf(
                "videoRecording" to "failed",
                "jobSlug" to "FunctionalTests"
            )


            val runAllFilterName = "runAll"
            filters.register(runAllFilterName)

            filters.register("dynamicFilter") {
                val includeAnnotation: String? = project.getOptionalStringProperty("includeAnnotation", true)
                if (includeAnnotation != null) {
                    fromSource.includeByAnnotations(setOf(includeAnnotation))
                }
                val includePrefix: String? = project.getOptionalStringProperty("includePrefix", true)
                if (includePrefix != null) {
                    fromSource.includeByPrefixes(setOf(includePrefix))
                }
            }

            val defaultFilter = "default"
            val customFilter: String = project.getOptionalStringProperty("localFilter", defaultFilter)

            configurationsContainer.register("Local") {
                tryToReRunOnTargetBranch = false
                reportSkippedTests = true
                reportFlakyTests = false
                filter = customFilter

                targetsContainer.register("api27") {
                    deviceName = "API27"

                    scheduling = com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration().apply {
                        quota = com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration().apply {
                            retryCount = 1
                            minimumSuccessCount = 1
                        }

                        reservation = com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration()
                            .apply {
                            device = com.avito.instrumentation.reservation.request.Device.LocalEmulator.device(27)
                            maximum = 1
                            minimum = 1
                            testsPerEmulator = 1
                        }
                    }
                }
            }

            val credentials = project.kubernetesCredentials
            if (credentials is com.avito.utils.gradle.KubernetesCredentials.Service || credentials is com.avito.utils.gradle.KubernetesCredentials.Config) {
                configurationsContainer.register("ui") {
                    tryToReRunOnTargetBranch = false
                    reportSkippedTests = true
                    reportFlakyTests = true
                    filter = customFilter

                    targetsContainer.register("api22") {
                        deviceName = "API22"

                        scheduling = com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration()
                            .apply {
                            quota = com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration()
                                .apply {
                                retryCount = 1
                                minimumSuccessCount = 1
                            }

                            reservation = com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration()
                                .apply {
                                device = com.avito.instrumentation.reservation.request.Device.Emulator.Emulator22
                                maximum = 50
                                minimum = 2
                                testsPerEmulator = 3
                            }
                        }
                    }

                    targetsContainer.register("api27") {
                        deviceName = "API27"

                        scheduling = com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration()
                            .apply {
                            quota = com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration()
                                .apply {
                                retryCount = 1
                                minimumSuccessCount = 1
                            }

                            reservation = com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration()
                                .apply {
                                device = com.avito.instrumentation.reservation.request.Device.Emulator.Emulator27
                                maximum = 50
                                minimum = 2
                                testsPerEmulator = 3
                            }
                        }
                    }
                }

                configurationsContainer.register("uiDebug") {
                    tryToReRunOnTargetBranch = false
                    reportSkippedTests = false
                    reportFlakyTests = false
                    enableDeviceDebug = true
                    filter = customFilter

                    targetsContainer.register("api27") {
                        deviceName = "API27"

                        scheduling = com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration()
                            .apply {
                            quota = com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration()
                                .apply {
                                retryCount = 1
                                minimumSuccessCount = 1
                            }

                            reservation = com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration()
                                .apply {
                                device = com.avito.instrumentation.reservation.request.Device.Emulator.Emulator27
                                maximum = 1
                                minimum = 1
                                testsPerEmulator = 1
                            }
                        }
                    }
                }
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