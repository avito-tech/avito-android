import com.avito.instrumentation.InstrumentationTestsPlugin
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
import com.avito.instrumentation.reservation.request.Device.CloudEmulator
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.KubernetesCredentials.Service
import com.avito.utils.gradle.kubernetesCredentials

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

        testInstrumentationRunnerArguments(
            mapOf(
                "planSlug" to "DesingPlatformTestApp"
            )
        )
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

plugins.withType<InstrumentationTestsPlugin> {
    extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {

        logcatTags = setOf(
            "ViewSaver:*"
        ) + logcatTags

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

                targetsContainer.register("api28") {
                    deviceName = "API28"

                    scheduling {
                        quota {
                            minimumSuccessCount = 1
                        }

                        staticDevicesReservation {
                            device = emulator28
                            count = 1
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
    implementation(Dependencies.material)
    implementation(Dependencies.androidTest.espressoCore)
    implementation("com.avito.android:test-screenshot")

    androidTestImplementation(project(":samples:test-app-core"))
    androidTestImplementation("com.avito.android:test-annotations")

    androidTestRuntimeOnly(Dependencies.playServicesMaps)
    androidTestUtil(Dependencies.androidTest.orchestrator)
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




