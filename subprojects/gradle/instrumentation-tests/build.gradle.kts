import com.avito.android.test.applyOptionalSystemProperty

plugins {
    id("convention.kotlin-jvm")
    id("convention.test-fixtures")
    id("convention.publish-gradle-plugin")
    id("convention.integration-testing")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    api(project(":gradle:kubernetes"))
    api(project(":common:time"))
    api(project(":gradle:build-verdict-tasks-api"))

    implementation(libs.commonsIo) {
        because("LogcatBuffer.Impl.tailer needs to consider Charset (https://issues.apache.org/jira/browse/IO-354)")
    }
    implementation(libs.commonsText)
    implementation(libs.coroutinesCore)
    implementation(libs.gson)
    implementation(libs.kotson)
    implementation(libs.retrofit)
    implementation(libs.teamcityClient)
    implementation(project(":common:composite-exception"))
    implementation(project(":common:file-storage"))
    implementation(project(":common:logger"))
    implementation(project(":common:report-viewer"))
    implementation(project(":common:retrace"))
    implementation(project(":common:http-client"))
    implementation(project(":common:throwable-utils"))
    implementation(project(":common:result"))
    implementation(project(":common:sentry"))
    implementation(project(":common:test-annotations"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":common:files"))
    implementation(project(":gradle:build-environment"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:instrumentation-changed-tests-finder"))
    implementation(project(":gradle:instrumentation-tests-dex-loader"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:process"))
    implementation(project(":gradle:runner:report"))
    implementation(project(":gradle:runner:client"))
    implementation(project(":gradle:runner:device-provider"))
    implementation(project(":gradle:runner:stub"))
    implementation(project(":gradle:teamcity"))
    implementation(project(":gradle:upload-cd-build-result"))
    implementation(project(":gradle:build-failer"))
    implementation(project(":gradle:worker"))

    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":gradle:build-failer-test-fixtures"))
    testImplementation(project(":gradle:instrumentation-tests-dex-loader-test-fixtures"))
    testImplementation(testFixtures(project(":common:http-client")))

    gradleTestImplementation(project(":gradle:test-project"))

    integTestImplementation(project(":common:statsd"))

    testFixturesApi(testFixtures(project(":common:logger")))
    testFixturesApi(testFixtures(project(":common:time")))
    testFixturesApi(testFixtures(project(":common:report-viewer")))
    testFixturesApi(testFixtures(project(":gradle:runner:device-provider")))
    testFixturesApi(testFixtures(project(":gradle:runner:report")))
    testFixturesApi(testFixtures(project(":gradle:runner:client")))
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
