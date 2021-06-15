plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    api(project(":gradle:build-verdict-tasks-api"))
    api(project(":gradle:kubernetes"))

    implementation(libs.gson)
    implementation(project(":common:time"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":gradle:build-failer"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":gradle:upload-cd-build-result"))
    implementation(project(":gradle:worker"))
    implementation(project(":logger:gradle-logger"))
    implementation(project(":logger:logger"))
    implementation(project(":test-runner:instrumentation-changed-tests-finder"))
    implementation(project(":test-runner:instrumentation-tests-dex-loader"))
    implementation(project(":test-runner:report"))
    implementation(project(":test-runner:test-annotations"))
    implementation(project(":test-runner:device-provider"))
    implementation(project(":test-runner:client"))

    testImplementation(project(":common:result"))
    testImplementation(project(":test-runner:report-api"))
    testImplementation(project(":common:http-client"))
    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":gradle:build-failer-test-fixtures"))
    testImplementation(testFixtures(project(":common:http-client")))
    testImplementation(testFixtures(project(":test-runner:report-api")))
    testImplementation(testFixtures(project(":test-runner:report-viewer")))
    testImplementation(testFixtures(project(":test-runner:client")))
    testImplementation(testFixtures(project(":test-runner:device-provider")))
    testImplementation(testFixtures(project(":test-runner:instrumentation-tests-dex-loader")))
    testImplementation(testFixtures(project(":test-runner:report")))

    gradleTestImplementation(project(":gradle:test-project"))
    gradleTestImplementation(project(":common:truth-extensions"))
}

kotlin {
    explicitApi()
}

gradlePlugin {
    plugins {
        create("functionalTests") {
            id = "com.avito.android.instrumentation-tests"
            implementationClass = "com.avito.instrumentation.InstrumentationTestsPlugin"
            displayName = "Instrumentation tests"
        }
    }
}
