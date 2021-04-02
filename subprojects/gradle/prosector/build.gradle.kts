plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.gradle.gradleLogger)
    implementation(projects.gradle.android)
    implementation(projects.gradle.statsdConfig)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.common.httpClient)
    implementation(projects.common.okhttp)

    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttpLogging)

    gradleTestImplementation(projects.gradle.testProject)
    gradleTestImplementation(projects.common.testOkhttp)
}

gradlePlugin {
    plugins {
        create("prosector") {
            id = "com.avito.android.prosector"
            implementationClass = "ProsectorPlugin"
            displayName = "Prosector"
        }
    }
}
