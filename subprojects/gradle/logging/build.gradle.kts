plugins {
    id("kotlin")
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
}
