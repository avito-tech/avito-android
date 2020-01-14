plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val gsonVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation(project(":utils"))
}
