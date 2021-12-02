plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

publish {
    artifactId.set("critical-path-api")
}

dependencies {
    api(projects.subprojects.common.graph)
    api(projects.subprojects.common.result)

    implementation(gradleApi())
    implementation(projects.subprojects.gradle.gradleExtensions)
    implementation(projects.subprojects.assemble.gradleProfile)
    implementation(projects.subprojects.common.compositeException)
    implementation(projects.subprojects.common.problem)
}
