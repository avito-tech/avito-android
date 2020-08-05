import com.android.build.gradle.ProguardFiles.ProguardFile
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.instrumentation.reservation.request.Device
import com.avito.instrumentation.reservation.request.Device.CloudEmulator
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.KubernetesCredentials.Service
import com.avito.utils.gradle.kubernetesCredentials
import com.avito.instrumentation.configuration.InstrumentationFilter.FromRunHistory.RunStatus

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.avito.android.design-screenshots")
    id("com.avito.android.instrumentation-tests")
}

android {

    defaultConfig {
        versionName = "1.0"
        versionCode = 1
        testInstrumentationRunner = "com.avito.android.test.app.core.TestAppRunner"

        // TODO: describe in docs that they are updated in IDE configuration only after sync!
        testInstrumentationRunnerArguments(
            mapOf(
                "planSlug" to "AndroidTestApp"
            )
        )
    }

    buildTypes {
        register("screenshot") {
            initWith(named("debug").get())
            setMatchingFallbacks("debug")

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(getDefaultProguardFile(ProguardFile.OPTIMIZE.fileName), "proguard-rules.pro")
        }
    }

    /**
     * Disable all buildTypes except testing
     * to avoid confusing errors in IDE if wrong build variant is selected
     */
    variantFilter {
        if (name != testBuildType) {
            ignore = true
            logger.debug("Build variant $name is omitted for module: $path")
        }
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

project(":samples:test-app-screenshot-test") {
    plugins.withType<com.avito.instrumentation.InstrumentationTestsPlugin> {
        extensions.getByType<com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration>()
            .apply {
                filters.register("screenshot") {
                    fromSource.includeByAnnotations(
                        setOf(
                            "com.avito.android.test.annotations.ScreenshotTest",
                            "com.avito.android.test.annotations.UIComponentTest"
                        )
                    )
                    fromRunHistory.excludePreviousStatuses(setOf(RunStatus.Success, RunStatus.Manual))
                }

                val credentials = project.kubernetesCredentials
                if (credentials is Service || credentials is KubernetesCredentials.Config) {
                    val emulator28 = CloudEmulator(
                        name = "api28",
                        api = 28,
                        model = "Android_SDK_built_for_x86_64",
                        image = "${emulatorImage(registry, 28)}:37ac40d0fa",
                        cpuCoresRequest = "1",
                        cpuCoresLimit = "1.3"
                    )

                    configurationsContainer.register("screenshot") {
                        reportSkippedTests = false
                        reportFlakyTests = false
                        enableDeviceDebug = true
                        filter = "screenshot"

                        targetsContainer.register("api28") {
                            deviceName = "API28"

                            scheduling {
                                quota {
                                    retryCount = 1
                                    minimumSuccessCount = 1
                                }

                                testsCountBasedReservation {
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
}


/**
 * delegateClosureOf used because kotlin dsl accessor
 * `fun Project.dependencies(configuration: DependencyHandlerScope.() -> Unit)`
 * is somehow unavailable for this and only this module.
 * It probably related to our custom plugins applied, but don't know how to debug this issue right now
 */
dependencies(delegateClosureOf<DependencyHandler> {
    implementation("com.avito.android:proxy-toast")

    implementation(Dependencies.playServicesMaps)

    implementation(Dependencies.material)
    implementation(Dependencies.androidTest.core)
    implementation(Dependencies.androidTest.espressoCore)
    implementation("com.avito.android:test-screenshot")

    androidTestImplementation(project(":samples:test-app-core"))
    androidTestImplementation("com.avito.android:test-report")
    androidTestImplementation("com.avito.android:junit-utils")
    androidTestImplementation("com.avito.android:toast-rule")
    androidTestImplementation("com.avito.android:test-annotations")
    androidTestImplementation("com.avito.android:ui-testing-core")
    androidTestImplementation("com.avito.android:report-viewer")
    androidTestImplementation("com.avito.android:file-storage")
    androidTestImplementation("com.avito.android:okhttp")
    androidTestImplementation("com.avito.android:time")

    androidTestImplementation(Dependencies.androidTest.runner)
    androidTestUtil(Dependencies.androidTest.orchestrator)

    androidTestImplementation(Dependencies.okhttp)
    androidTestImplementation(Dependencies.okhttpLogging)
    androidTestImplementation(Dependencies.funktionaleTry)
    androidTestImplementation(Dependencies.kotson)
    androidTestImplementation(Dependencies.sentry)
    androidTestImplementation(Dependencies.test.mockitoCore)
    androidTestImplementation(Dependencies.test.truth)
    androidTestImplementation(Dependencies.test.okhttpMockWebServer)
})

gradle.taskGraph.whenReady {
    tasks.getByName<VerificationTask>("connectedDebugAndroidTest") {
        ignoreFailures = hasProperty("recordScreenshotsMode")
    }
}

//todo registry value not respected here, it's unclear how its used (in fact concatenated in runner)
//todo pass whole image, and not registry
fun emulatorImage(registry: String?, api: Int): String {
    return if (registry.isNullOrBlank()) {
        "avitotech/android-emulator-$api"
    } else {
        "android/emulator-$api"
    }
}




