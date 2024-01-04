plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(gradleApi())
    api(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:git"))
}
