plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(projects.subprojects.signer) {
        because("Creates qappsUploadSigned<Variant> tasks which is directly depends on corresponding signer task")
    }

    implementation(projects.subprojects.logger.logger)
    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.common.httpClient)
    implementation(projects.subprojects.common.result)
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.gradle.buildFailer)
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.logger.gradleLogger)
    implementation(projects.subprojects.gradle.statsdConfig)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttpLogging)

    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(projects.subprojects.common.testOkhttp)
    testImplementation(testFixtures(projects.subprojects.logger.logger))
    testImplementation(testFixtures(projects.subprojects.common.httpClient))

    gradleTestImplementation(projects.subprojects.gradle.testProject)
    gradleTestImplementation(projects.subprojects.common.testOkhttp)
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
