plugins {
    `kotlin-dsl`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(libs.kotlinPlugin)
    implementation(libs.nebulaIntegTest)
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
