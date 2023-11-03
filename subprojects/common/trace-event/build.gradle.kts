plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(libs.gson)

    testImplementation(libs.kotlinStdlib) {
        because("kotlin.io.path experimental; consider replacing")
    }
}
