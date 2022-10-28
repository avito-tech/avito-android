plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttpLogging)
    implementation(projects.subprojects.common.okhttp)
    implementation(projects.subprojects.common.result)
    implementation(projects.subprojects.logger.logger)

    testImplementation(projects.subprojects.common.testOkhttp)
}
