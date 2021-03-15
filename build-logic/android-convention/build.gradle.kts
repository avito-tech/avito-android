plugins {
    `kotlin-dsl`
    id("convention.libraries")
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation("com.avito.android.buildlogic:libraries")
    implementation("com.avito.android.buildlogic:kotlin-convention")
    implementation(libs.kotlinPlugin)
    implementation(libs.androidGradlePlugin)
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
