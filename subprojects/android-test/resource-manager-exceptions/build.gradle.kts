plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(libs.kotlinStdlib)
    implementation(project(":common:report-viewer"))
}
