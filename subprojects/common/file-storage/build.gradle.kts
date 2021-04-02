plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(project(":common:time"))
    implementation(project(":common:logger"))
    implementation(project(":common:okhttp"))
    implementation(project(":common:http-client"))

    implementation(libs.kotlinStdlib)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
}
