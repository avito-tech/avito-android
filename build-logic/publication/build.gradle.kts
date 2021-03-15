plugins {
    `kotlin-dsl`
    id("convention.libraries")
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(libs.androidGradlePlugin)
    implementation(libs.okhttp)
    implementation(libs.kotson)
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
