plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

publish {
    artifactId.set("runner-client")
}

dependencies {
    compileOnly(gradleApi())
    api(project(":subprojects:test-runner:service"))
    api(project(":subprojects:test-runner:report-viewer")) {
        because("ReportViewerConfig exposes ReportCoordinates; also RunId")
    }
    api(project(":subprojects:test-runner:test-suite-provider"))

    implementation(project(":subprojects:common:composite-exception"))
    implementation(project(":subprojects:common:coroutines-extension"))
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:common:math"))
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:common:result"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:common:trace-event"))
    implementation(project(":subprojects:test-runner:device-provider:api"))
    implementation(project(":subprojects:test-runner:device-provider:impl"))
    implementation(project(":subprojects:test-runner:instrumentation-tests-dex-loader"))
    implementation(project(":subprojects:test-runner:report-processor"))
    implementation(project(":subprojects:test-runner:runner-api"))
    implementation(project(":subprojects:test-runner:test-annotations"))
    implementation(project(":subprojects:test-runner:test-report-artifacts"))
    implementation(project(":subprojects:test-runner:report-viewer-test-static-data-parser"))
    implementation(project(":subprojects:test-runner:inhouse-avito-report"))
    implementation(libs.coroutinesCore)
    implementation(libs.gson)
    implementation(libs.commonsText) {
        because("for StringEscapeUtils.escapeXml10() only")
    }

    testImplementation(libs.coroutinesTest)
    testImplementation(libs.kotlinReflect)
    testImplementation(libs.mockitoJUnitJupiter)
    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(testFixtures(project(":subprojects:common:time")))
    testImplementation(project(":subprojects:logger:logger"))
    testImplementation(testFixtures(project(":subprojects:test-runner:instrumentation-tests-dex-loader")))
    testImplementation(testFixtures(project(":subprojects:test-runner:report")))
    testImplementation(testFixtures(project(":subprojects:test-runner:report-viewer")))
    testImplementation(testFixtures(project(":subprojects:test-runner:service")))
    testImplementation(testFixtures(project(":subprojects:test-runner:device-provider:model")))

    testFixturesImplementation(project(":subprojects:logger:logger"))
    testFixturesImplementation(testFixtures(project(":subprojects:common:time")))
    testFixturesImplementation(testFixtures(project(":subprojects:test-runner:device-provider:impl")))
    testFixturesImplementation(testFixtures(project(":subprojects:test-runner:report")))
    testFixturesImplementation(testFixtures(project(":subprojects:test-runner:service")))
    testFixturesImplementation(testFixtures(project(":subprojects:test-runner:test-suite-provider")))
}
