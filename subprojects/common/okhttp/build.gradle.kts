plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(libs.okhttp)

    implementation(project(":subprojects:common:logger"))
    implementation(libs.okhttpLogging)

    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(libs.funktionaleTry)
    testImplementation(libs.retrofit)
    testImplementation(libs.retrofitConverterGson)
}

kotlin {
    explicitApi()
}
