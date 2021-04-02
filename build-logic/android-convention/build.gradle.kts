plugins {
    `kotlin-dsl`
    id("convention.libraries")
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(projects.kotlinConvention)
    implementation("com.avito.android.buildlogic:libraries")
    implementation(libs.kotlinPlugin)
    implementation(libs.androidGradlePlugin)
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
