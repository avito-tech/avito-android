plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.gson)
    implementation(project(":subprojects:gradle:utils"))
}
