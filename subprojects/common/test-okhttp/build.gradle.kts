plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.okhttpMockWebServer)
    api(project(":subprojects:common:okhttp"))
    api(project(":subprojects:logger:logger"))

    implementation(libs.bundles.hamcrest)
    implementation(libs.truth)
    implementation(libs.kotson)
    implementation(libs.commonsLang)

    implementation(project(":subprojects:common:junit-utils"))
    implementation(project(":subprojects:common:resources"))
    implementation(project(":subprojects:common:waiter"))
    implementation(project(":subprojects:common:result"))

    implementation(libs.jsonAssert)
}
