plugins {
    `kotlin-dsl`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation("com.avito.android.buildlogic:testing-convention")
    implementation(libs.kotlinPlugin)
    implementation(libs.nebulaIntegTest)
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
