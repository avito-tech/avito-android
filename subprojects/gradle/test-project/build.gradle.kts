plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.gradle-testing")
}

dependencies {
    api(gradleTestKit())

    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:common:truth-extensions"))
    implementation(project(":subprojects:logger:logger"))

    implementation(libs.androidToolsCommon)
    implementation(libs.kotlinReflect)
    implementation(libs.truth)
}
