plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.gradle.git)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.logger.gradleLogger)

    // required for gradleTest code
    implementation(projects.subprojects.testRunner.instrumentationTests)

    gradleTestImplementation(projects.subprojects.gradle.testProject)
    gradleTestImplementation(projects.subprojects.common.truthExtensions)
}

gradlePlugin {
    plugins {
        create("applyBaselineProfile") {
            id = "com.avito.android.apply-baseline-profile"
            implementationClass = "com.avito.android.baseline_profile.ApplyBaselineProfilePlugin"
            displayName = "Generate and apply baseline profile"
        }
    }
}
