plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.subprojects.common.files)
    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.common.httpClient)
    implementation(projects.subprojects.common.result)
    implementation(projects.subprojects.common.problem)
    implementation(projects.subprojects.common.throwableUtils)
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.gradle.worker)
    implementation(projects.subprojects.gradle.statsdConfig)
    implementation(projects.subprojects.gradle.buildFailer)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.logger.slf4jGradleLogger)

    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)

    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(projects.subprojects.common.testOkhttp)
    testImplementation(projects.subprojects.logger.logger)
    testImplementation(testFixtures(projects.subprojects.common.httpClient))

    gradleTestImplementation(projects.subprojects.gradle.testProject)
    gradleTestImplementation(projects.subprojects.common.testOkhttp)
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
