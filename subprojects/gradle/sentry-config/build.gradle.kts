plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
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
