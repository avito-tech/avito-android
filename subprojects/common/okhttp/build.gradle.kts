plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.okhttp)
    api(project(":subprojects:common:result"))

    implementation(project(":subprojects:logger:logger"))
    implementation(libs.okhttpLogging)

    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:common:result"))
    testImplementation(libs.retrofit)
    testImplementation(libs.retrofitConverterGson)
}
