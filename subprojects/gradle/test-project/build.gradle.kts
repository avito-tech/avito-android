plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.gradle-testing")
}

dependencies {
    api(libs.okhttpMockWebServer)
    api(gradleTestKit())

    implementation(projects.subprojects.gradle.process)
    implementation(projects.subprojects.gradle.android)
    implementation(projects.subprojects.common.truthExtensions)
    implementation(projects.subprojects.logger.logger)

    implementation(libs.androidToolsCommon)
    implementation(libs.kotlinReflect)
    implementation(libs.truth)

    testImplementation(libs.kotlinTest)
    testImplementation(libs.kotlinTestJUnit)
}
