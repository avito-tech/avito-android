plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":gradle:process"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":logger:gradle-logger"))
    implementation(libs.gson)

    testImplementation(testFixtures(project(":logger:logger")))

    gradleTestImplementation(project(":gradle:test-project"))
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
