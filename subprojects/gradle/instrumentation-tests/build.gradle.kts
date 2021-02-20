plugins {
    id("convention.kotlin-jvm")
    id("convention.test-fixtures")
    id("convention.publish-gradle-plugin")
    id("convention.integration-testing")
    id("convention.libraries")
    id("convention.gradle-testing")
}

dependencies {
    constraints {
        api(libs.okio) {
            version { strictly("2.+") }
            because(
                "There was an issue dependency-analysis plugin which depends on 1.x version of okio, " +
                    "caused runtime failures"
            )
        }
    }

    api(project(":subprojects:gradle:kubernetes"))
    api(project(":subprojects:common:time"))
    api(project(":subprojects:gradle:build-verdict-tasks-api"))

    implementation(libs.commonsIo) {
        because("LogcatBuffer.Impl.tailer needs to consider Charset (https://issues.apache.org/jira/browse/IO-354)")
    }
    implementation(libs.commonsText)
    implementation(libs.coroutinesCore)
    implementation(libs.funktionaleTry)
    implementation(libs.gson)
    implementation(libs.kotson)
    implementation(libs.retrofit)
    implementation(libs.teamcityClient)
    implementation(project(":subprojects:common:composite-exception"))
    implementation(project(":subprojects:common:file-storage"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:retrace"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:common:test-annotations"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:instrumentation-changed-tests-finder"))
    implementation(project(":subprojects:gradle:instrumentation-tests-dex-loader"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:runner:report"))
    implementation(project(":subprojects:gradle:runner:client"))
    implementation(project(":subprojects:gradle:runner:device-provider"))
    implementation(project(":subprojects:gradle:runner:stub"))
    implementation(project(":subprojects:gradle:teamcity"))
    implementation(project(":subprojects:gradle:upload-cd-build-result"))
    implementation(project(":subprojects:gradle:build-failer"))
    implementation(project(":subprojects:gradle:worker"))

    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(project(":subprojects:gradle:build-failer-test-fixtures"))
    testImplementation(project(":subprojects:gradle:instrumentation-tests-dex-loader-test-fixtures"))

    gradleTestImplementation(project(":subprojects:gradle:test-project"))

    integTestImplementation(project(":subprojects:common:statsd"))

    testFixturesApi(testFixtures(project(":subprojects:common:logger")))
    testFixturesApi(testFixtures(project(":subprojects:common:time")))
    testFixturesApi(testFixtures(project(":subprojects:common:report-viewer")))
    testFixturesApi(testFixtures(project(":subprojects:gradle:runner:device-provider")))
    testFixturesApi(testFixtures(project(":subprojects:gradle:runner:report")))
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

fun Test.applyOptionalSystemProperty(name: String) {
    project.property(name)?.toString()?.let { value -> systemProperty(name, value) }
}
