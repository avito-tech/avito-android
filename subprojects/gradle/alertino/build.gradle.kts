plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttpLogging)
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:logger:logger"))

    testImplementation(project(":subprojects:common:test-okhttp"))
}
