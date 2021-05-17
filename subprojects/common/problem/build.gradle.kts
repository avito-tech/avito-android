plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(libs.jdoctorCore)
    api(libs.jdoctorUtils)
}
