plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    api(project(":subprojects:common:sentry"))

    implementation(gradleApi())
    implementation(project(":subprojects:common:okhttp"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(libs.funktionaleTry)
}
