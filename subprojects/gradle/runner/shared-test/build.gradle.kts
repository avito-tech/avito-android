plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

publish {
    artifactId.set("runner-shared-test")
}

dependencies {
    api(project(":gradle:runner:stub"))
    api(libs.coroutinesTest)

    compileOnly(gradleApi())

    implementation(libs.coroutinesCore)
    implementation(libs.kotson)
    implementation(project(":common:report-viewer"))
    implementation(project(":test-runner:service"))
    implementation(project(":test-runner:shared"))
    implementation(project(":gradle:test-project"))
}
