plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
}

extra["artifact-id"] = "runner-shared-test"

dependencies {
    api(project(":subprojects:gradle:runner:stub"))
    api(libs.coroutinesTest)

    compileOnly(gradleApi())

    implementation(libs.coroutinesCore)
    implementation(libs.funktionaleTry)
    implementation(libs.kotson)
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:gradle:runner:service"))
    implementation(project(":subprojects:gradle:runner:shared"))
    implementation(project(":subprojects:gradle:test-project"))
}
