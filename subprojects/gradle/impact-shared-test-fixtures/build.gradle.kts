plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.libraries")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:impact-shared"))
    implementation(project(":subprojects:gradle:test-project"))
    implementation(libs.truth)
}
