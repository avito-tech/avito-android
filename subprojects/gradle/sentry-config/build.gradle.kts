plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(project(":common:sentry"))

    implementation(gradleApi())
    implementation(project(":common:build-metadata"))
    implementation(project(":common:okhttp"))
    implementation(project(":common:logger"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":gradle:gradle-extensions"))
}
