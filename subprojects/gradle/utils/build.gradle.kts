plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(gradleApi())
    api(project(":subprojects:gradle:kotlin-dsl-support"))

    testImplementation(Dependencies.test.truth)
    testImplementation(Dependencies.test.mockitoKotlin)
}
