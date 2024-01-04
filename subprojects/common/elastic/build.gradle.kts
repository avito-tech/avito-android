plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.okhttp)

    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:okhttp"))

    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)

    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:logger:logger"))
    testImplementation(testFixtures(project(":subprojects:common:time")))
}
