plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.common.files)
    implementation(projects.common.okhttp)
    implementation(projects.common.httpClient)
    implementation(projects.common.result)
    implementation(projects.common.problem)
    implementation(projects.common.throwableUtils)
    implementation(projects.gradle.android)
    implementation(projects.gradle.worker)
    implementation(projects.gradle.statsdConfig)
    implementation(projects.gradle.buildFailer)
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.logger.gradleLogger)

    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)

    testImplementation(projects.common.truthExtensions)
    testImplementation(projects.common.testOkhttp)
    testImplementation(testFixtures(projects.logger.logger))
    testImplementation(testFixtures(projects.common.httpClient))

    gradleTestImplementation(projects.gradle.testProject)
    gradleTestImplementation(projects.common.testOkhttp)
}

gradlePlugin {
    plugins {
        create("signer") {
            id = "com.avito.android.signer"
            implementationClass = "com.avito.plugin.SignServicePlugin"
            displayName = "Signer"
        }
    }
}
