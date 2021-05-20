plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(project(":common:result"))

    implementation(project(":common:time"))
    implementation(project(":logger:logger"))
    implementation(project(":common:okhttp"))
    implementation(project(":common:http-client"))

    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
}

kotlin {
    explicitApi()
}
