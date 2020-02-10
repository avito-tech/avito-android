plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val gsonVersion: String by project

dependencies {
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation(project(":subprojects:gradle:utils"))
}
