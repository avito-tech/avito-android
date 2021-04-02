plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

publish {
    artifactId.set("runner-shared-test")
}

dependencies {
    api(projects.gradle.runner.stub)
    api(libs.coroutinesTest)

    compileOnly(gradleApi())

    implementation(libs.coroutinesCore)
    implementation(libs.kotson)
    implementation(projects.common.reportViewer)
    implementation(projects.gradle.runner.service)
    implementation(projects.gradle.runner.shared)
    implementation(projects.gradle.testProject)
}
