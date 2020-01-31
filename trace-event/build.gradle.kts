plugins {
    id("kotlin")
    `maven-publish`
}

val gsonVersion: String by project

dependencies {
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation(project(":utils"))
}
