plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project

dependencies {
    implementation(project(":report-viewer"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}
