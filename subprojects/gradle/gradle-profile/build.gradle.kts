plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:gradle-extensions"))
}
