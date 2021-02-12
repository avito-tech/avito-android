plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(libs.okhttp)

    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:slf4j-logger"))

    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)

    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(testFixtures(project(":subprojects:common:time")))
    testImplementation(testFixtures(project(":subprojects:common:logger")))
}

kotlin {
    explicitApi()
}
