plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

extra["artifact-id"] = "runner-shared"

dependencies {
    compileOnly(gradleApi())
    api(project(":subprojects:common:logger"))
    implementation(libs.rxJava)
}
