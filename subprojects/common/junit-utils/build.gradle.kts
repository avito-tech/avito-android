plugins {
    id("convention.kotlin-jvm-android")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(libs.junit)
    implementation(libs.truth)
    implementation(libs.bundles.hamcrest)
}

kotlin {
    explicitApiWarning()
}
