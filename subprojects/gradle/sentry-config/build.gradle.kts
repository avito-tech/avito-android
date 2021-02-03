plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:common:sentry"))

    implementation(gradleApi())
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(Dependencies.funktionaleTry)
}
