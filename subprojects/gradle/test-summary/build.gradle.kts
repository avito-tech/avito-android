plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":common:report-viewer"))
    implementation(project(":common:time"))
    implementation(project(":gradle:ci-logger"))
    implementation(project(":gradle:slack"))
    implementation(project(":gradle:statsd-config"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.okhttp)

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":common:report-viewer-test-fixtures"))
}

gradlePlugin {
    plugins {
        create("testSummary") {
            id = "com.avito.android.test-summary"
            implementationClass = "com.avito.test.summary.TestSummaryPlugin"
            displayName = "Instrumentation tests summary"
        }
    }
}
