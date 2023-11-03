plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.testRunner.testReport)
    api(projects.subprojects.testRunner.report)
}
