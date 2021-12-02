plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    compileOnly(gradleApi())

    implementation(projects.subprojects.gradle.gradleExtensions)
}
