plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

publish {
    artifactId.set("runner-stub")
}

dependencies {
    implementation(project(":common:report-viewer"))
    implementation(project(":gradle:runner:service"))
    implementation(project(":gradle:runner:shared"))

    implementation(libs.kotlinStdlib)
}
