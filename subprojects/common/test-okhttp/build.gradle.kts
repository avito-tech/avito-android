plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(libs.okhttpMockWebServer)
    api(project(":subprojects:common:logger"))

    implementation(libs.truth)
    implementation(libs.gson)
    implementation(libs.commonsLang)

    implementation(project(":subprojects:common:junit-utils"))
    implementation(project(":subprojects:common:resources"))
    implementation(project(":subprojects:common:waiter"))

    testImplementation(testFixtures(project(":subprojects:common:logger")))
}
