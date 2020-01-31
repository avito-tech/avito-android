plugins {
    id("kotlin")
    `maven-publish`
}

dependencies {
    implementation(project(":kotlin-dsl-support"))
    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(gradleApi())
    implementation("com.google.apis:google-api-services-androidpublisher:v3-rev86-1.25.0")
}
