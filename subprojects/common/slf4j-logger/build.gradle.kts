plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:common:logger"))

    implementation(Dependencies.slf4jApi)
}
