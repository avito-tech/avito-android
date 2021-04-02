plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(gradleApi())
    api(projects.gradle.gradleExtensions)
    implementation(projects.common.logger)
    implementation(projects.gradle.git)
}
