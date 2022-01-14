plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.okhttp)

    implementation(projects.subprojects.common.time)
    implementation(projects.subprojects.common.okhttp)

    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)

    testImplementation(projects.subprojects.common.testOkhttp)
    testImplementation(projects.subprojects.logger.logger)
    testImplementation(testFixtures(projects.subprojects.common.time))
}
