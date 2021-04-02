plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    api(libs.okhttpMockWebServer)
    api(gradleTestKit())

    implementation(projects.gradle.process)
    implementation(projects.gradle.android)
    implementation(projects.common.truthExtensions)
    implementation(testFixtures(projects.common.logger))

    implementation(libs.kotlinReflect)
    implementation(libs.truth)

    testImplementation(libs.kotlinTest)
    testImplementation(libs.kotlinTestJUnit)
}
