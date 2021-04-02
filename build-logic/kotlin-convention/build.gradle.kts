plugins {
    `kotlin-dsl`
    id("convention.libraries")
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(projects.testingConvention)
    implementation("com.avito.android.buildlogic:libraries")
    implementation(libs.kotlinPlugin)
    implementation(libs.nebulaIntegTest)
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}
