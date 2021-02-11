plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
}

dependencies {
    api(project(":subprojects:common:graphite"))

    implementation(gradleApi())
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
}
