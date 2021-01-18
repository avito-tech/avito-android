plugins {
    id("kotlin")
}

dependencies {
    api(project(":gradle:instrumentation-tests"))
    api(project(":gradle:instrumentation-tests-dex-loader"))
    api(project(":common:report-viewer-test-fixtures"))
    api(project(":common:logger-test-fixtures"))
    api(project(":common:time-test-fixtures"))

    implementation(project(":gradle:bitbucket"))
    implementation(project(":common:report-viewer"))
    implementation(project(":gradle:slack"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":gradle:process"))
    implementation(project(":gradle:process-test-fixtures"))
    implementation(project(":gradle:kubernetes"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:test-project"))
    implementation(project(":gradle:runner:service")) {
        because("to access Adb class")
    }
    implementation(project(":gradle:runner:client")) {
        because("to access TestLifecycleListener class")
    }
}
