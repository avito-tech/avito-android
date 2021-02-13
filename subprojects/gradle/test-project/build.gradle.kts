plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(libs.okhttpMockWebServer)
    api(gradleTestKit())

    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:common:truth-extensions"))
    implementation(testFixtures(project(":subprojects:common:logger")))

    implementation(libs.kotlinReflect)
    implementation(libs.funktionaleTry)
    implementation(libs.truth)

    testImplementation(libs.kotlinTest)
    testImplementation(libs.kotlinTestJUnit)
}
