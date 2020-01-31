plugins {
    id("kotlin")
    `maven-publish`
}

val statsdVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation("com.timgroup:java-statsd-client:$statsdVersion")

    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
}
