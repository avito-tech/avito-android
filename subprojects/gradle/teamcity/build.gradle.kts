plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.common.teamcityCommon)
    implementation(gradleApi())
    implementation(projects.subprojects.gradle.gradleExtensions)

    testImplementation(projects.subprojects.gradle.testProject)
}
