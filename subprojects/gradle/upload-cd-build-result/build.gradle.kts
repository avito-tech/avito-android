plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.gson)

    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:gradle-extensions"))

    testImplementation(project(":subprojects:common:test-okhttp"))
    testImplementation(testFixtures(project(":subprojects:common:logger")))
    testImplementation(project(":subprojects:gradle:git-test-fixtures"))
    testImplementation(project(":subprojects:gradle:test-project"))
}
