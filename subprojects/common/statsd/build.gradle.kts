plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":subprojects:common:series"))
    implementation(libs.statsd)
    implementation(project(":subprojects:logger:logger"))
}
