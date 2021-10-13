plugins {
    id("convention.kotlin-jvm")
}

dependencies {
    api(projects.subprojects.gradle.git)
}
