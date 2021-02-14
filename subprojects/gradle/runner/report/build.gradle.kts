plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
    `java-test-fixtures`
}

extra["artifact-id"] = "runner-report"

dependencies {
    api(project(":subprojects:common:report-viewer"))

    implementation(project(":subprojects:common:time"))
    implementation(testFixtures(project(":subprojects:common:report-viewer")))

    testFixturesImplementation(testFixtures(project(":subprojects:common:logger")))
    testFixturesImplementation(testFixtures(project(":subprojects:common:time")))
    testFixturesImplementation(testFixtures(project(":subprojects:common:report-viewer")))
}

kotlin {
    explicitApi()

    /**
     * Workaround to access internal classes from testFixtures
     * till https://youtrack.jetbrains.com/issue/KT-34901 resolved
     */
    target.compilations
        .matching { it.name in listOf("testFixtures") }
        .configureEach {
            associateWith(target.compilations.getByName("main"))
        }
}
