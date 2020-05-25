plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:common:sentry"))

    implementation(gradleApi())
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(Dependencies.funktionaleTry)
}

