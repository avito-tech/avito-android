plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

dependencies {
    implementation(project(":common:files"))
    implementation(project(":common:time"))
    implementation(project(":common:composite-exception"))
    implementation(project(":common:report-viewer"))
    implementation(project(":common:report-viewer"))

    implementation(project(":gradle:build-failer"))

    implementation(project(":test-runner:report"))
}

kotlin {
    explicitApi()
}
