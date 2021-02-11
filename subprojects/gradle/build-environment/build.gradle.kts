plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(gradleApi())
    api(project(":subprojects:gradle:gradle-extensions"))
}
