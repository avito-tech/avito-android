plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:logger:gradle-logger"))

    // required for gradleTest code
    implementation(project(":subprojects:test-runner:instrumentation-tests"))

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    gradleTestImplementation(project(":subprojects:common:truth-extensions"))
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
