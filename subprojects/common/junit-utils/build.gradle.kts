plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    id("com.avito.android.libraries")
}

dependencies {
    implementation(libs.junit)
    implementation(libs.truth)
    implementation(libs.hamcrestLib)

    testImplementation(libs.kotlinReflect)
}
