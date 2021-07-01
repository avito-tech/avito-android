plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.gradle-testing")
}

dependencies {
    api(libs.okhttpMockWebServer)
    api(gradleTestKit())

    implementation(project(":gradle:process"))
    implementation(project(":gradle:android"))
    implementation(project(":common:truth-extensions"))
    implementation(testFixtures(project(":logger:logger")))

    implementation(libs.kotlinReflect)
    implementation(libs.truth)

    testImplementation(libs.kotlinTest)
    testImplementation(libs.kotlinTestJUnit)
}
