plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    implementation(libs.statsd)
    implementation(project(":subprojects:common:logger"))
}
