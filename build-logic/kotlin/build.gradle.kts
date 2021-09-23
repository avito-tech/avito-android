plugins {
    `kotlin-dsl`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(projects.gradle)
    implementation(projects.testing)
    implementation(libs.kotlinGradle)
    implementation(libs.nebulaIntegTest)
}
