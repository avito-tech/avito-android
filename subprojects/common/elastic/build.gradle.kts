plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(libs.okhttp)

    implementation(project(":common:time"))
    implementation(project(":common:okhttp"))
    implementation(project(":logger:slf4j-logger"))

    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)

    testImplementation(project(":common:test-okhttp"))
    testImplementation(testFixtures(project(":common:time")))
    testImplementation(testFixtures(project(":logger:logger")))
}

kotlin {
    explicitApi()
}
