plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(gradleApi())
    api(project(":gradle:kotlin-dsl-support"))
    api(project(":common:logger"))

    testImplementation(Dependencies.test.truth)
    testImplementation(Dependencies.test.mockitoKotlin)
}
