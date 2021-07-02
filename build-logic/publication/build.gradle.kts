plugins {
    `kotlin-dsl`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation("com.avito.android.buildlogic:checks")
    implementation(libs.androidGradlePlugin)
    implementation(libs.okhttp)
    implementation(libs.kotson)
}
