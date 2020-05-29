plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(Dependencies.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(Dependencies.okhttp)
    api(project(":gradle:utils"))
    api(project(":gradle:build-environment"))
    implementation(project(":gradle:sentry-config"))
    implementation(project(":gradle:kotlin-dsl-support"))
}
