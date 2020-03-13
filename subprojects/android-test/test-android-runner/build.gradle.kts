plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
    id("digital.wup.android-maven-publish")
}

dependencies {
    api(Dependencies.androidTest.runner)
    api(Dependencies.androidTest.espressoCore)
    api(Dependencies.androidTest.espressoCore)
    api(Dependencies.appcompat)
    api(project(":subprojects:android-test:test-report"))
    api(project(":subprojects:common:report-viewer"))
    api(project(":subprojects:common:sentry"))
}
