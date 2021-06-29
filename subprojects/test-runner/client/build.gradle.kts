plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("runner-client")
}

dependencies {
    compileOnly(gradleApi())
    api(project(":test-runner:service"))
    api(project(":common:result"))

    implementation(project(":test-runner:test-annotations"))
    implementation(project(":test-runner:instrumentation-tests-dex-loader"))
    implementation(project(":test-runner:test-report-artifacts"))
    implementation(project(":test-runner:report"))
    implementation(project(":test-runner:device-provider:impl"))
    implementation(project(":gradle:trace-event"))
    implementation(project(":common:throwable-utils"))
    implementation(project(":common:math"))
    implementation(project(":common:composite-exception"))
    implementation(project(":common:result"))
    implementation(project(":common:problem"))
    implementation(project(":common:files"))
    implementation(project(":common:retrace"))
    implementation(project(":test-runner:file-storage"))
    implementation(libs.coroutinesCore)
    implementation(libs.gson)
    implementation(libs.commonsText) {
        because("for StringEscapeUtils.escapeXml10() only")
    }
    implementation(libs.commonsIo) {
        because("LogcatBuffer.Impl.tailer needs to consider Charset (https://issues.apache.org/jira/browse/IO-354)")
    }

    testImplementation(project(":common:truth-extensions"))
    testImplementation(project(":gradle:test-project"))
    testImplementation(testFixtures(project(":logger:logger")))
    testImplementation(testFixtures(project(":common:time")))
    testImplementation(testFixtures(project(":test-runner:report-api")))
    testImplementation(testFixtures(project(":test-runner:report-viewer")))
    testImplementation(testFixtures(project(":test-runner:service")))
    testImplementation(testFixtures(project(":test-runner:instrumentation-tests-dex-loader")))
    testImplementation(libs.kotlinReflect)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(libs.coroutinesTest)

    testFixturesImplementation(testFixtures(project(":test-runner:device-provider:impl")))
    testFixturesImplementation(testFixtures(project(":test-runner:report")))
    testFixturesImplementation(testFixtures(project(":test-runner:service")))
    testFixturesImplementation(testFixtures(project(":logger:logger")))
    testFixturesImplementation(testFixtures(project(":common:time")))
}

kotlin {
    explicitApi()
}
