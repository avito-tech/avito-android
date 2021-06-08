import com.avito.android.test.applyOptionalSystemProperty

plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.integration-testing")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    api(project(":common:time"))
    api(project(":gradle:build-verdict-tasks-api"))
    api(project(":gradle:kubernetes"))

    implementation(libs.commonsText)
    implementation(libs.coroutinesCore)
    implementation(libs.gson)
    implementation(libs.kotson)
    implementation(libs.retrofit)
    implementation(libs.teamcityClient)
    implementation(project(":common:composite-exception"))
    implementation(project(":common:file-storage"))
    implementation(project(":common:files"))
    implementation(project(":common:http-client"))
    implementation(project(":common:problem"))
    implementation(project(":common:report-api"))
    implementation(project(":common:result"))
    implementation(project(":common:retrace"))
    implementation(project(":common:sentry"))
    implementation(project(":common:throwable-utils"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":gradle:build-failer"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:process"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":gradle:teamcity"))
    implementation(project(":gradle:upload-cd-build-result"))
    implementation(project(":gradle:worker"))
    implementation(project(":logger:gradle-logger"))
    implementation(project(":logger:logger"))
    implementation(project(":test-runner:client"))
    implementation(project(":test-runner:device-provider"))
    implementation(project(":test-runner:instrumentation-changed-tests-finder"))
    implementation(project(":test-runner:instrumentation-tests-dex-loader"))
    implementation(project(":test-runner:report"))
    implementation(project(":test-runner:stub"))
    implementation(project(":test-runner:test-annotations"))
    implementation(project(":test-runner:test-report-artifacts"))

    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":gradle:build-failer-test-fixtures"))
    testImplementation(testFixtures(project(":common:http-client")))
    testImplementation(testFixtures(project(":common:report-api")))
    testImplementation(testFixtures(project(":common:report-viewer")))
    testImplementation(testFixtures(project(":test-runner:client")))
    testImplementation(testFixtures(project(":test-runner:device-provider")))
    testImplementation(testFixtures(project(":test-runner:instrumentation-tests-dex-loader")))
    testImplementation(testFixtures(project(":test-runner:report")))

    gradleTestImplementation(project(":gradle:test-project"))

    integTestImplementation(project(":common:statsd"))
    integTestImplementation(testFixtures(project(":test-runner:client")))
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

tasks.named<Test>("integrationTest").configure {
    applyOptionalSystemProperty("kubernetesUrl")
    applyOptionalSystemProperty("kubernetesToken")
    applyOptionalSystemProperty("kubernetesCaCertData")
    applyOptionalSystemProperty("kubernetesNamespace")
}
