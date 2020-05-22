plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.avito.android.instrumentation-tests")
}

android {

    defaultConfig {
        versionName = "1.0"
        versionCode = 1
    }

    /**
     * Disable all buildTypes except testing
     * to avoid confusing errors in IDE if wrong build variant is selected
     */
    variantFilter {
        if (name != testBuildType) {
            setIgnore(true)
            logger.debug("Build variant $name is omitted for module: $path")
        }
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

dependencies {

    implementation(Dependencies.playServicesMaps)

    implementation(Dependencies.appcompat)
    implementation(Dependencies.recyclerView)
    implementation(Dependencies.material)

    androidTestImplementation(project(":subprojects:android-test:test-inhouse-runner"))
    androidTestImplementation(project(":subprojects:android-test:test-report"))
    androidTestImplementation(project(":subprojects:android-test:junit-utils"))
    androidTestImplementation(project(":subprojects:android-test:toast-rule"))
    androidTestImplementation(project(":subprojects:android-test:test-annotations"))
    androidTestImplementation(project(":subprojects:android-test:ui-testing-core"))
    androidTestImplementation(project(":subprojects:common:report-viewer"))
    androidTestImplementation(project(":subprojects:common:file-storage"))
    androidTestImplementation(project(":subprojects:common:okhttp"))
    androidTestImplementation(project(":subprojects:common:time"))

    androidTestImplementation(Dependencies.androidTest.runner)
    androidTestUtil(Dependencies.androidTest.orchestrator)

    androidTestImplementation(Dependencies.test.junit)
    androidTestImplementation(Dependencies.okhttp)
    androidTestImplementation(Dependencies.okhttpLogging)
    androidTestImplementation(Dependencies.funktionaleTry)
    androidTestImplementation(Dependencies.gson)
    androidTestImplementation(Dependencies.kotson)
    androidTestImplementation(Dependencies.sentry)
    androidTestImplementation(Dependencies.test.truth)
    androidTestImplementation(Dependencies.test.okhttpMockWebServer)
}
