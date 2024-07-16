plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
}

dependencies {
    implementation(libs.okhttp)
    implementation(project(":subprojects:common:http-statsd"))
    implementation(project(":subprojects:common:math"))
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:alertino"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:logger:slf4j-gradle-logger"))
    implementation(project(":subprojects:test-runner:report-viewer")) {
        because("API to fetch reports; ReportCoordinates model")
    }
    implementation(project(":subprojects:logger:gradle-logger"))

    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(testFixtures(project(":subprojects:test-runner:report-viewer")))

    gradleTestImplementation(project(":subprojects:gradle:test-project"))

    testFixturesImplementation(testFixtures(project(":subprojects:test-runner:test-model")))
}

gradlePlugin {
    plugins {
        create("testSummary") {
            id = "com.avito.android.tests-summary"
            implementationClass = "com.avito.test.summary.TestSummaryPlugin"
            displayName = "Instrumentation tests summary"
        }
    }
}
