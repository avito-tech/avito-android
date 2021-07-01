plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(libs.gson)

    testImplementation(libs.kotlinStdlibJdk7) {
        because("kotlin.io.path experimental; consider replacing")
    }
}
