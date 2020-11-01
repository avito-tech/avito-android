plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":common:sentry"))

    implementation(gradleApi())
    implementation(project(":common:okhttp"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(Dependencies.funktionaleTry)
}

