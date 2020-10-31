plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.AndroidTest.core)
    api(Dependencies.AndroidTest.espressoCore)
    api(Dependencies.AndroidTest.espressoWeb)
    api(Dependencies.AndroidTest.espressoIntents)
    api(Dependencies.AndroidTest.uiAutomator)

    api(Dependencies.AndroidTest.espressoDescendantActions)

    api(Dependencies.appcompat)
    api(Dependencies.recyclerView)
    api(Dependencies.material)

    //todo implementation, waitForAssertion used in app
    api(project(":common:waiter"))

    implementation(Dependencies.Test.hamcrestLib)
    implementation(Dependencies.Test.junit)
    implementation(Dependencies.freeReflection)
}
