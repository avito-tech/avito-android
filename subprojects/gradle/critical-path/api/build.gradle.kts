plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

publish {
    artifactId.set("critical-path-api")
}

dependencies {
    api(projects.common.graph)
    api(projects.common.result)

    implementation(gradleApi())
    implementation(projects.gradle.gradleExtensions)
    implementation(projects.gradle.gradleProfile)
    implementation(projects.common.compositeException)
    implementation(projects.common.problem)
    implementation(projects.logger.gradleLogger)
}

kotlin {
    explicitApi()
}
