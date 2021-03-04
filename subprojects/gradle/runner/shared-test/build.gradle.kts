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
    implementation(libs.funktionaleTry)
    implementation(libs.kotson)
    implementation(project(":common:report-viewer"))
    implementation(project(":gradle:runner:service"))
    implementation(project(":gradle:runner:shared"))
    implementation(project(":gradle:test-project"))
}
