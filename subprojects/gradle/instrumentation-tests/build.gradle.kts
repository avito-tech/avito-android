plugins {
    id("java-gradle-plugin")
    id("kotlin")
    id("java-test-fixtures")
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
    testImplementation(testFixtures(project(":subprojects:gradle:logging")))
    testImplementation(testFixtures(project(":subprojects:gradle:slack")))
    testImplementation(testFixtures(project(":subprojects:gradle:utils")))
    testImplementation(testFixtures(project(":subprojects:common:report-viewer")))
    testImplementation(Dependencies.test.mockitoKotlin)
    testImplementation(Dependencies.test.mockitoJUnitJupiter)
    testImplementation(Dependencies.test.okhttpMockWebServer)

    testFixturesImplementation(project(":subprojects:gradle:kubernetes"))
    testFixturesImplementation(project(":subprojects:gradle:utils"))
    testFixturesImplementation(project(":subprojects:gradle:test-project"))
    testFixturesImplementation(project(":subprojects:gradle:bitbucket"))
    testFixturesImplementation(project(":subprojects:gradle:slack"))
    testFixturesImplementation(project(":subprojects:common:statsd"))
    testFixturesImplementation(project(":subprojects:common:report-viewer"))
    testFixturesImplementation(Dependencies.test.okhttpMockWebServer)
    testFixturesImplementation(Dependencies.funktionaleTry)
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
