@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.avito.instrumentation.InstrumentationTestsPlugin
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
import com.avito.instrumentation.configuration.target.scheduling.SchedulingConfiguration
import com.avito.instrumentation.configuration.target.scheduling.quota.QuotaConfiguration
import com.avito.instrumentation.configuration.target.scheduling.reservation.TestsBasedDevicesReservationConfiguration
import com.avito.instrumentation.reservation.request.Device
import com.avito.instrumentation.reservation.request.Device.CloudEmulator
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.KubernetesCredentials.Service
import com.avito.utils.gradle.kubernetesCredentials
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

    plugins.withType<com.android.build.gradle.AppPlugin> {
        configure<com.android.build.gradle.BaseExtension> {
            packagingOptions {
                exclude("META-INF/*.kotlin_module")
            }
        }
    }

    plugins.matching { it is AppPlugin || it is LibraryPlugin }.whenPluginAdded {
        configure<BaseExtension> {
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

    plugins.withType<InstrumentationTestsPlugin> {
        extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {

            //todo make these params optional features in plugin
            reportApiUrl = project.getOptionalStringProperty("avito.report.url") ?: "http://stub"
            reportApiFallbackUrl = project.getOptionalStringProperty("avito.report.fallbackUrl") ?: "http://stub"
            reportViewerUrl = project.getOptionalStringProperty("avito.report.viewerUrl") ?: "http://stub"
            registry = project.getOptionalStringProperty("avito.registry", "registry") ?: "registry"
            sentryDsn = project.getOptionalStringProperty("avito.instrumentaion.sentry.dsn")
                ?: "http://stub-project@stub-host/0"
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

                targetsContainer.register("api28") {
                    deviceName = "API28"

                    scheduling = SchedulingConfiguration().apply {
                        quota = QuotaConfiguration().apply {
                            retryCount = 1
                            minimumSuccessCount = 1
                        }

                        reservation = TestsBasedDevicesReservationConfiguration().apply {
                            // Replace 27 with 28 when 2020.6.1 will be released
                            //device = com.avito.instrumentation.reservation.request.Device.LocalEmulator.device(28, "Android_SDK_built_for_x86_64")
                            device = Device.LocalEmulator.device(27)
                            maximum = 1
                            minimum = 1
                            testsPerEmulator = 1
                        }
                    }
                }
            }

            val credentials = project.kubernetesCredentials
            if (credentials is Service || credentials is KubernetesCredentials.Config) {

                val registry = project.providers.gradleProperty("avito.registry").orNull

                val emulator22 = CloudEmulator(
                    name = "api22",
                    api = 22,
                    model = "Android_SDK_built_for_x86",
                    image = "${emulatorImage(registry, 22)}:24e6ed0ec4",
                    cpuCoresRequest = "1",
                    cpuCoresLimit = "1.3"
                )

                val emulator28 = CloudEmulator(
                    name = "api28",
                    api = 28,
                    model = "Android_SDK_built_for_x86_64",
                    image = "${emulatorImage(registry, 28)}:a9b53d28be",
                    cpuCoresRequest = "1",
                    cpuCoresLimit = "1.3"
                )

                configurationsContainer.register("ui") {
                    tryToReRunOnTargetBranch = false
                    reportSkippedTests = true
                    reportFlakyTests = true
                    filter = customFilter

                    targetsContainer.register("api22") {
                        deviceName = "API22"

                        scheduling = SchedulingConfiguration().apply {
                            quota = QuotaConfiguration().apply {
                                retryCount = 1
                                minimumSuccessCount = 1
                            }

                            reservation = TestsBasedDevicesReservationConfiguration().apply {
                                device = emulator22
                                maximum = 50
                                minimum = 2
                                testsPerEmulator = 3
                            }
                        }
                    }

                    targetsContainer.register("api28") {
                        deviceName = "API28"

                        scheduling = SchedulingConfiguration().apply {
                            quota = QuotaConfiguration().apply {
                                retryCount = 1
                                minimumSuccessCount = 1
                            }

                            reservation = TestsBasedDevicesReservationConfiguration().apply {
                                device = emulator28
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

                    targetsContainer.register("api28") {
                        deviceName = "API28"

                        scheduling = SchedulingConfiguration().apply {
                            quota = QuotaConfiguration().apply {
                                retryCount = 1
                                minimumSuccessCount = 1
                            }

                            reservation = TestsBasedDevicesReservationConfiguration().apply {
                                device = emulator28
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

    plugins.withType<KotlinBasePluginWrapper> {
        this@subprojects.run {
            tasks {
                withType<KotlinCompile> {
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

fun emulatorImage(registry: String?, api: Int): String {
    return if (registry.isNullOrBlank()) {
        "avitotech/android-emulator-$api"
    } else {
        //todo registry value not respected here, it's unclear how its used (in fact concatenated in runner)
        "android/emulator-$api"
    }
}
