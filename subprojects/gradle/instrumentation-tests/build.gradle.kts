plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
    id("nebula.integtest")
}

dependencies {
    api(project(":gradle:kubernetes"))
    api(project(":common:time"))
    api(project(":gradle:build-verdict-tasks-api"))

    implementation(Dependencies.commonsIo) {
        because("LogcatBuffer.Impl.tailer needs to consider Charset (https://issues.apache.org/jira/browse/IO-354)")
    }
    implementation(Dependencies.commonsText)
    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.gson)
    implementation(Dependencies.kotson)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.teamcityClient)
    implementation(project(":common:composite-exception"))
    implementation(project(":common:file-storage"))
    implementation(project(":common:logger"))
    implementation(project(":common:report-viewer"))
    implementation(project(":common:retrace"))
    implementation(project(":common:sentry"))
    implementation(project(":common:test-annotations"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:gradle-logger"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":common:files"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:instrumentation-test-impact-analysis"))
    implementation(project(":gradle:instrumentation-changed-tests-finder"))
    implementation(project(":gradle:instrumentation-tests-dex-loader"))
    implementation(project(":gradle:gradle-extensions"))
    implementation(project(":gradle:process"))
    implementation(project(":gradle:runner:client"))
    implementation(project(":gradle:runner:stub"))
    implementation(project(":gradle:teamcity"))
    implementation(project(":gradle:upload-cd-build-result"))
    implementation(project(":gradle:build-failer"))
    implementation(project(":gradle:worker"))

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":gradle:slack-test-fixtures"))
    testImplementation(project(":gradle:build-failer-test-fixtures"))
    testImplementation(project(":common:logger-test-fixtures"))
    testImplementation(project(":common:time-test-fixtures"))
    testImplementation(project(":gradle:instrumentation-tests-dex-loader-test-fixtures"))
    testImplementation(project(":common:report-viewer-test-fixtures"))
    testImplementation(project(":common:resources"))
    testImplementation(Dependencies.Test.mockitoKotlin)
    testImplementation(Dependencies.Test.mockitoJUnitJupiter)
    testImplementation(Dependencies.Test.okhttpMockWebServer)
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
