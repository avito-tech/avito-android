plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.gradle.process)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.logger.gradleLogger)
    implementation(libs.gson)

    testImplementation(testFixtures(projects.logger.logger))

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
