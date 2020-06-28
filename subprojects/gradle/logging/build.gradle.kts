plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":gradle:utils"))

    implementation(gradleApi())
    implementation(Dependencies.slackClient) { exclude(group = "com.squareup.okhttp3") }
    implementation(Dependencies.okhttp)
    implementation(project(":gradle:sentry-config"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:build-environment"))
}
