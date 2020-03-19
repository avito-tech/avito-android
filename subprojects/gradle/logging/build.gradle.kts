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
    api(project(":subprojects:gradle:utils"))
    api(project(":subprojects:gradle:build-environment"))
    implementation(project(":subprojects:gradle:sentry-config"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))

    testFixturesImplementation(Dependencies.kotlinStdlib)
    testFixturesImplementation(project(":subprojects:gradle:utils"))
}
