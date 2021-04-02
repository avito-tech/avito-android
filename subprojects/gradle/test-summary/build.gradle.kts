plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    implementation(project(":common:report-viewer"))
    implementation(project(":common:time"))
    implementation(project(":common:math"))
    implementation(project(":common:http-client"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:slack"))
    implementation(project(":gradle:statsd-config"))
    implementation(libs.okhttp)
    implementation(libs.kotlinStdlib)

    testImplementation(project(":common:truth-extensions"))
    testImplementation(testFixtures(project(":common:report-viewer")))

    gradleTestImplementation(project(":gradle:test-project"))
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
