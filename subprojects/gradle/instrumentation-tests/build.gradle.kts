plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":gradle:kubernetes"))
    api(project(":common:time"))

    implementation(Dependencies.coroutinesCore)
    implementation(project(":gradle:instrumentation-test-impact-analysis"))
    implementation(project(":gradle:runner:client"))
    implementation(project(":gradle:utils"))
    implementation(project(":gradle:ci-logger"))
    implementation(project(":gradle:android"))
    implementation(project(":gradle:teamcity"))
    implementation(project(":gradle:statsd-config"))
    implementation(project(":gradle:test-summary"))
    implementation(project(":common:report-viewer"))
    implementation(project(":gradle:kotlin-dsl-support"))
    implementation(project(":gradle:git"))
    implementation(project(":gradle:process"))
    implementation(project(":gradle:files"))
    implementation(project(":gradle:slack"))
    implementation(project(":gradle:bitbucket"))
    implementation(project(":common:file-storage"))
    implementation(project(":common:sentry"))
    implementation(project(":common:logger"))
    implementation(project(":common:retrace"))
    implementation(project(":common:test-annotations"))
    implementation(project(":gradle:upload-cd-build-result"))
    implementation(project(":gradle:worker"))
    implementation(Dependencies.dexlib)
    implementation(Dependencies.gson)
    implementation(Dependencies.teamcityClient)
    implementation(Dependencies.commonsText)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.kotson)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.commonsIo) {
        because("LogcatBuffer.Impl.tailer needs to consider Charset (https://issues.apache.org/jira/browse/IO-354)")
    }

    testImplementation(project(":gradle:test-project"))
    testImplementation(project(":gradle:logging-test-fixtures"))
    testImplementation(project(":gradle:slack-test-fixtures"))
    testImplementation(project(":gradle:utils-test-fixtures"))
    testImplementation(project(":gradle:instrumentation-tests-test-fixtures"))
    testImplementation(project(":common:report-viewer-test-fixtures"))
    testImplementation(Dependencies.test.mockitoKotlin)
    testImplementation(Dependencies.test.mockitoJUnitJupiter)
    testImplementation(Dependencies.test.okhttpMockWebServer)
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
