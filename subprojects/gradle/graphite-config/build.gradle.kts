plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.common.graphite)

    implementation(gradleApi())
    implementation(projects.subprojects.gradle.gradleExtensions)
}
