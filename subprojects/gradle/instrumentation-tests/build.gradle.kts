plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.coroutinesCore)

    implementation(project(":subprojects:gradle:instrumentation-test-impact-analysis"))
    implementation(project(":subprojects:gradle:runner:client"))
    implementation(project(":subprojects:gradle:utils"))
    implementation(project(":subprojects:gradle:logging"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:teamcity"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:gradle:test-summary"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:gradle:kotlin-dsl-support"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:files"))
    implementation(project(":subprojects:gradle:slack"))
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:gradle:bitbucket"))
    implementation(project(":subprojects:common:file-storage"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:gradle:kubernetes"))
    implementation(project(":subprojects:gradle:upload-cd-build-result"))
    implementation(Dependencies.dexlib)
    implementation(Dependencies.gson)
    implementation(Dependencies.teamcityClient)
    implementation(Dependencies.commonsText)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.kotson)
    implementation(Dependencies.funktionaleTry)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:logging-test-fixtures"))
    testImplementation(project(":subprojects:gradle:slack-test-fixtures"))
    testImplementation(project(":subprojects:gradle:utils-test-fixtures"))
    testImplementation(project(":subprojects:gradle:instrumentation-tests-test-fixtures"))
    testImplementation(project(":subprojects:common:report-viewer-test-fixtures"))
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
