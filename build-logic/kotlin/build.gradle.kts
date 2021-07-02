plugins {
    `kotlin-dsl`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation("com.avito.android.buildlogic:testing")
    implementation(libs.kotlinPlugin)
    implementation(libs.nebulaIntegTest)
}
