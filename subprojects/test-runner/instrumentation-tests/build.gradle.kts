plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
}

dependencies {
    api(project(":subprojects:assemble:build-verdict-tasks-api"))
    api(project(":subprojects:test-runner:kubernetes"))

    implementation(libs.gson)
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:build-failer"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:worker-extensions"))
    implementation(project(":subprojects:logger:gradle-logger"))
    implementation(project(":subprojects:logger:logger"))
    implementation(project(":subprojects:test-runner:client"))
    implementation(project(":subprojects:test-runner:device-provider:model"))
    implementation(project(":subprojects:test-runner:instrumentation-changed-tests-finder"))
    implementation(project(":subprojects:test-runner:plugins-configuration"))
    implementation(project(":subprojects:test-runner:report"))
    implementation(project(":subprojects:test-runner:report-viewer"))
    implementation(project(":subprojects:test-runner:test-annotations"))
    implementation(project(":subprojects:test-runner:test-suite-provider"))

    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(testFixtures(project(":subprojects:test-runner:report")))
    testImplementation(testFixtures(project(":subprojects:test-runner:report-viewer")))
    testImplementation(testFixtures(project(":subprojects:test-runner:client")))
    testImplementation(testFixtures(project(":subprojects:test-runner:instrumentation-tests-dex-loader")))

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    gradleTestImplementation(project(":subprojects:common:truth-extensions"))
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
