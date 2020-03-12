plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(Dependencies.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(Dependencies.okhttp)
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:sentry-config"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))

    testFixturesImplementation(Dependencies.kotlinStdlib)
    testFixturesImplementation(project(":subprojects:gradle:utils"))
}
