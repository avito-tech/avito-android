plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    api(gradleApi())

    implementation(libs.kotlinStdlib)
}
