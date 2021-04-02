plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.common.statsd)

    implementation(gradleApi())
    implementation(projects.gradle.gradleLogger)
    implementation(projects.gradle.gradleExtensions)
}
