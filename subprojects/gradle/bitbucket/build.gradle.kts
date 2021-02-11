plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    implementation(gradleApi())

    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.sentry)
    implementation(libs.funktionaleTry)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(testFixtures(project(":subprojects:common:logger")))
}
