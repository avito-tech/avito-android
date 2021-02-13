plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:logger"))

    implementation(libs.retrofit)
    implementation(libs.okhttp)
}
