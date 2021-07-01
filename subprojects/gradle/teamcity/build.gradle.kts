plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.common.teamcityCommon)
    implementation(gradleApi())
    implementation(projects.gradle.gradleExtensions)

    testImplementation(projects.gradle.testProject)
}
