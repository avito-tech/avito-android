plugins {
    id("kotlin")
    `maven-publish`
}

val statsdVersion: String by project

dependencies {
    implementation(gradleApi())
    implementation("com.timgroup:java-statsd-client:$statsdVersion")

    implementation(project(":utils"))
    implementation(project(":logging"))
    implementation(project(":kotlin-dsl-support"))
}
