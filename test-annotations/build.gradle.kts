plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project
val gsonVersion: String by project
val junitVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
