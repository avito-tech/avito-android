plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

val bcelVersion: String by project
val gsonVersion: String by project

dependencies {
    implementation("org.apache.bcel:bcel:$bcelVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")

    testImplementation(project(":subprojects:gradle:test-project"))
}
