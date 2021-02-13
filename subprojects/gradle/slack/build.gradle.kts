plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("nebula.integtest")
    id("com.avito.android.libraries")
}

dependencies {
    api(libs.funktionaleTry)

    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:common:time"))
    implementation(libs.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(libs.okhttp)
    implementation(libs.coroutinesCore)

    integTestImplementation(project(":subprojects:gradle:gradle-extensions"))

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:slack-test-fixtures"))
    testImplementation(testFixtures(project(":subprojects:common:time")))
    testImplementation(testFixtures(project(":subprojects:common:logger")))
}
