plugins {
    `kotlin-dsl`
}

group = "com.avito.android.buildlogic"

dependencies {
    implementation(projects.gradleExt)
    implementation(projects.testing)
    implementation(libs.kotlinGradle)
    implementation(libs.nebulaIntegTest)
}
