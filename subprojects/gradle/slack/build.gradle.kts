plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":subprojects:common:result"))

    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:okhttp"))
    implementation(libs.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(libs.okhttp)
    implementation(libs.coroutinesCore)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:common:statsd")))
    testImplementation(testFixtures(project(":subprojects:common:time")))
    testImplementation(testFixtures(project(":subprojects:gradle:slack")))
}
