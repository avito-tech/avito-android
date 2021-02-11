plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
}

dependencies {
    implementation(gradleApi())
    implementation(project(":subprojects:gradle:gradle-extensions"))
}
