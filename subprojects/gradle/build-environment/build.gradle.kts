plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(gradleApi())
    api(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.gradle.git)
}
