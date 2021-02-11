plugins {
    id("com.avito.android.kotlin-android-library")
    id("com.avito.android.publish-android-library")
    id("com.avito.android.libraries")
}

dependencies {
    api(libs.androidXTestCore)
    api(libs.espressoCore)
    api(libs.espressoWeb)
    api(libs.espressoIntents)
    api(libs.uiAutomator)
    api(libs.espressoDescendantActions)
    api(libs.appcompat)
    api(libs.recyclerView)
    api(libs.material)

    // todo implementation, waitForAssertion used in app
    api(project(":subprojects:common:waiter"))

    implementation(libs.hamcrestLib)
    implementation(libs.junit)
    implementation(libs.freeReflection)
}
