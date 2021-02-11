plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
}

dependencies {
    api(gradleApi())
    api(project(":subprojects:gradle:gradle-extensions"))
}
