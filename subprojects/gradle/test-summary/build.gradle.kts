plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-gradle-plugin")
    id("com.avito.android.libraries")
}

dependencies {
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:time"))
    implementation(project(":subprojects:common:math"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:slack"))
    implementation(libs.funktionaleTry)
    implementation(libs.okhttp)

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
