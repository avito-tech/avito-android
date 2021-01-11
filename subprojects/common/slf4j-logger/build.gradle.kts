plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":common:logger"))

    implementation(Dependencies.slf4jApi)
}
