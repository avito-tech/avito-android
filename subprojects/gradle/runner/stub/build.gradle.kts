plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

publish {
    artifactId.set("runner-stub")
}

dependencies {
    implementation(project(":common:report-viewer"))
    implementation(project(":test-runner:service"))
    implementation(project(":gradle:runner:shared"))
}
