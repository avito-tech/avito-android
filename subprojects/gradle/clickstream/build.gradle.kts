plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.kotlin-serialization")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.gson)
    implementation(libs.okhttp)
    api(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.converter.kotlinx.serialization)
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
}
