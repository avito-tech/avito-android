plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
}

extra["artifact-id"] = "runner-shared"

dependencies {
    compileOnly(gradleApi())
    api(project(":subprojects:common:logger"))
    implementation(libs.rxJava)
}
