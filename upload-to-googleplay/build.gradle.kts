plugins {
    id("kotlin")
    `maven-publish`
}

val kotlinVersion: String by project

dependencies {
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.google.apis:google-api-services-androidpublisher:v3-rev86-1.25.0")
}
