plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    compileOnly(gradleApi())
    api(project(":common:logger"))
    implementation(project(":gradle:sentry-config"))
    implementation(project(":gradle:build-environment"))
}
