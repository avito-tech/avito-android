plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.funktionaleTry)

    implementation(project(":gradle:gradle-logger"))
    implementation(project(":common:time"))
    implementation(Dependencies.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(Dependencies.okhttp)
    implementation(Dependencies.coroutinesCore)

//    integTestImplementation(project(":gradle:gradle-extensions"))

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":gradle:slack-test-fixtures"))
    testImplementation(project(":common:time-test-fixtures"))
    testImplementation(project(":common:logger-test-fixtures"))
}
