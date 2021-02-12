plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:logger"))

    implementation(libs.retrofit)
    implementation(libs.okhttp)
}
