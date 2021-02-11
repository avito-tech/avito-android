plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-gradle-plugin")
    id("com.avito.android.libraries")
}

dependencies {
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(libs.funktionaleTry)
    implementation(libs.gson)

    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(project(":subprojects:gradle:test-project"))
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
