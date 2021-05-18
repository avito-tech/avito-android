plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

publish {
    artifactId.set("runner-shared-test")
}

dependencies {
    api(project(":test-runner:stub"))
    api(libs.coroutinesTest)

    compileOnly(gradleApi())

    implementation(libs.coroutinesCore)
    implementation(libs.kotson)
    implementation(project(":common:report-viewer"))
    implementation(project(":test-runner:service"))
    implementation(project(":gradle:test-project"))
}
