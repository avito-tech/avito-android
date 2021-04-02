plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(project(":common:report-viewer"))

    implementation(libs.kotlinStdlib)
}
