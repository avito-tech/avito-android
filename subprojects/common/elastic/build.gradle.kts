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
    testImplementation(testFixtures(projects.subprojects.common.time))
    testImplementation(testFixtures(projects.subprojects.logger.logger))
}
