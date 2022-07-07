plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.testRunner.reportViewer)

    implementation(gradleApi())
    api(projects.subprojects.gradle.buildEnvironment)
    api(projects.subprojects.gradle.git)
}
