plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(project(":common:result"))

    implementation(gradleApi())
    implementation(project(":gradle:impact-shared"))
    implementation(project(":gradle:test-project"))
    implementation(libs.truth)
}
