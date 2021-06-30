plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(project(":test-runner:report-viewer"))
    implementation(project(":common:time"))
    implementation(project(":common:math"))
    implementation(project(":common:problem"))
    implementation(project(":common:http-client"))
    implementation(project(":logger:gradle-logger"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:slack"))
    implementation(project(":gradle:statsd-config"))
    implementation(libs.okhttp)

    testImplementation(project(":common:truth-extensions"))
    testImplementation(testFixtures(project(":test-runner:report-viewer")))

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
