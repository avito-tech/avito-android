plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.okhttp)
    api(project(":common:result"))

    implementation(project(":logger:logger"))
    implementation(libs.okhttpLogging)

    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":common:test-okhttp"))
    testImplementation(project(":common:result"))
    testImplementation(testFixtures(project(":logger:logger")))
    testImplementation(libs.retrofit)
    testImplementation(libs.retrofitConverterGson)
}

kotlin {
    explicitApi()
}
