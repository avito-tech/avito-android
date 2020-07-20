plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.androidTest.core)
    api(Dependencies.androidTest.espressoCore)
    api(Dependencies.androidTest.espressoWeb)
    api(Dependencies.androidTest.espressoIntents)
    api(Dependencies.androidTest.uiAutomator)

    api(Dependencies.androidTest.espressoDescendantActions)

    api(Dependencies.appcompat)
    api(Dependencies.recyclerView)
    api(Dependencies.material)

    //todo implementation, waitForAssertion used in app
    api(project(":common:waiter"))

    implementation(Dependencies.test.hamcrestLib)
    implementation(Dependencies.test.junit)
    implementation(Dependencies.freeReflection)
}
