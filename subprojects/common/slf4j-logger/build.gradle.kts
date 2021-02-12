plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(project(":subprojects:common:logger"))

    implementation(libs.slf4jApi)
}
