plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.okhttpMockWebServer)
    api(project(":logger:logger"))
    api(project(":common:okhttp"))

    implementation(libs.truth)
    implementation(libs.kotson)
    implementation(libs.commonsLang)

    implementation(project(":common:junit-utils"))
    implementation(project(":common:resources"))
    implementation(project(":common:waiter"))
    implementation(project(":common:result"))

    testImplementation(testFixtures(project(":logger:logger")))
}

kotlin {
    explicitApi()
}
