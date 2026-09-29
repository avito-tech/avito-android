plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.kotlin-serialization")
    id("convention.unit-testing")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:gradle:build-environment"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:mtls"))
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.okhttpMockWebServer)
    testImplementation(libs.okhttpTls)
    testImplementation(project(":subprojects:common:test-okhttp"))
}
