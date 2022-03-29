plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(projects.subprojects.common.result)
    implementation(projects.subprojects.common.commandLineCoroutines)
    implementation(libs.adam)
}
