plugins {
    `kotlin-dsl`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(libs.androidGradle)
    implementation(libs.okhttp)
    implementation(libs.kotson)
}
