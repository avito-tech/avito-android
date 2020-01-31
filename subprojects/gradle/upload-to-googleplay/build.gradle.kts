plugins {
    id("kotlin")
    `maven-publish`
}

dependencies {
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(gradleApi())
    implementation("com.google.apis:google-api-services-androidpublisher:v3-rev86-1.25.0")
}
