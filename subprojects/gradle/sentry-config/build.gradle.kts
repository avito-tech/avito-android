plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":common:sentry"))

    implementation(gradleApi())
    implementation(project(":common:okhttp"))
    implementation(project(":common:logger"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(Dependencies.funktionaleTry)
}
