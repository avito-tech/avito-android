plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("com.avito.android.libraries")
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
