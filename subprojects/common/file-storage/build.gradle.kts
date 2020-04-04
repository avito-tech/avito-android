plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:logger"))

    implementation(Dependencies.retrofit)
    implementation(Dependencies.okhttp)
}
