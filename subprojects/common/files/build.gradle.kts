plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.common.result)
    api(libs.androidAnnotations)
}
