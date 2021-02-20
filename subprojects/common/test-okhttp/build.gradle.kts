plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(libs.okhttpMockWebServer)
    api(project(":subprojects:common:logger"))
    api(project(":subprojects:common:okhttp"))

    implementation(libs.truth)
    implementation(libs.gson)
    implementation(libs.commonsLang)

    implementation(project(":subprojects:common:junit-utils"))
    implementation(project(":subprojects:common:resources"))
    implementation(project(":subprojects:common:waiter"))

    testImplementation(testFixtures(project(":subprojects:common:logger")))
}
