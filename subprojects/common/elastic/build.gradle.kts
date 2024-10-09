plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.okhttp)
    api(project(":subprojects:common:time"))
    api(project(":subprojects:common:okhttp"))
    api(project(":subprojects:logger:logger"))

    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)

    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(project(":subprojects:logger:logger"))
    testImplementation(testFixtures(project(":subprojects:common:time")))
}
