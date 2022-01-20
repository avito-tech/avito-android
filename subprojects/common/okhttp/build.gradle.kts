plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.okhttp)
    api(projects.subprojects.common.result)

    implementation(projects.subprojects.logger.logger)
    implementation(libs.okhttpLogging)

    testImplementation(projects.subprojects.common.truthExtensions)
    testImplementation(projects.subprojects.common.testOkhttp)
    testImplementation(projects.subprojects.common.result)
    testImplementation(libs.retrofit)
    testImplementation(libs.retrofitConverterGson)
}
