plugins {
    id("kotlin")
    `maven-publish`
}

val statsdVersion: String by project

dependencies {
    api(project(":subprojects:common:statsd"))

    implementation(gradleApi())
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
}
