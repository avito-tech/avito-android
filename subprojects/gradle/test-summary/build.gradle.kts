plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:math"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:slack"))
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.okhttp)

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(testFixtures(project(":subprojects:common:report-viewer")))
}

gradlePlugin {
    plugins {
        create("testSummary") {
            id = "com.avito.android.test-summary"
            implementationClass = "com.avito.test.summary.TestSummaryPlugin"
            displayName = "Instrumentation tests summary"
        }
    }
}
