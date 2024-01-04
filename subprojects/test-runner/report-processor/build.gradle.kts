plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.test-fixtures")
}

dependencies {
    api(project(":subprojects:test-runner:test-model"))
    api(project(":subprojects:test-runner:runner-api"))
    api(project(":subprojects:test-runner:report"))
    api(project(":subprojects:logger:logger"))

    implementation(project(":subprojects:test-runner:test-report-artifacts"))
    implementation(project(":subprojects:test-runner:file-storage"))
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:common:retrace"))
    implementation(project(":subprojects:common:throwable-utils"))
    implementation(libs.coroutinesCore)
    implementation(libs.commonsIo) {
        because("LogcatBuffer.Impl.tailer needs to consider Charset (https://issues.apache.org/jira/browse/IO-354)")
    }

    testImplementation(project(":subprojects:common:truth-extensions"))
    testImplementation(testFixtures(project(":subprojects:common:time")))
    testImplementation(testFixtures(project(":subprojects:test-runner:report")))
    testImplementation(testFixtures(project(":subprojects:test-runner:runner-api")))
}
