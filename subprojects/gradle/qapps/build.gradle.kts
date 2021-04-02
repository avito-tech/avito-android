plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.common.logger)
    implementation(projects.common.okhttp)
    implementation(projects.common.httpClient)
    implementation(projects.common.result)
    implementation(projects.gradle.android)
    implementation(projects.gradle.buildFailer)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.gradleLogger)
    implementation(projects.gradle.statsdConfig)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttpLogging)

    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.common.testOkhttp)
    testImplementation(testFixtures(projects.common.logger))
    testImplementation(testFixtures(projects.common.httpClient))

    gradleTestImplementation(projects.gradle.testProject)
    gradleTestImplementation(projects.common.testOkhttp)
}

gradlePlugin {
    plugins {
        create("qapps") {
            id = "com.avito.android.qapps"
            implementationClass = "com.avito.plugin.QAppsPlugin"
            displayName = "QApps"
        }
    }
}
