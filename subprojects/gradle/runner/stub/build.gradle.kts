plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

publish {
    artifactId.set("runner-stub")
}

dependencies {
    implementation(projects.common.reportViewer)
    implementation(projects.gradle.runner.service)
    implementation(projects.gradle.runner.shared)
}
