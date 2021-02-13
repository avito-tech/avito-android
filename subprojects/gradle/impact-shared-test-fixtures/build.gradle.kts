plugins {
    id("convention.kotlin-jvm")
    id("convention.libraries")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:test-project"))
    implementation(libs.truth)
}
