plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
}

dependencies {
    implementation(libs.proguardRetrace)

    testImplementation(libs.junit)
}
