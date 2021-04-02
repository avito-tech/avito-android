plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":gradle:process"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:gradle-logger"))
    implementation(libs.gson)
    implementation(libs.kotlinStdlib)

    testImplementation(testFixtures(project(":common:logger")))

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
