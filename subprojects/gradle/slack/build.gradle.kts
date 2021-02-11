plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
    id("nebula.integtest")
}

dependencies {
    api(Dependencies.funktionaleTry)

    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:common:time"))
    implementation(Dependencies.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(Dependencies.okhttp)
    implementation(Dependencies.coroutinesCore)

    integTestImplementation(project(":subprojects:gradle:gradle-extensions"))

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:slack-test-fixtures"))
    testImplementation(project(":subprojects:common:time-test-fixtures"))
    testImplementation(project(":subprojects:common:logger-test-fixtures"))
}
