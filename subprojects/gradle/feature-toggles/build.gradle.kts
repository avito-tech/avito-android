plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.gradle.process)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.gradleLogger)
    implementation(libs.gson)

    testImplementation(testFixtures(projects.common.logger))

    gradleTestImplementation(projects.gradle.testProject)
}

gradlePlugin {
    plugins {
        create("featureTogglesReport") {
            id = "com.avito.android.feature-toggles"
            implementationClass = "com.avito.android.plugin.FeatureTogglesPlugin"
            displayName = "Feature-toggle reporter"
        }
    }
}
