plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    compileOnly(gradleApi())
    api(project(":subprojects:logger:logger"))
}
