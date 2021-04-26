plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

dependencies {
    api(libs.okhttp)

    implementation(libs.gson) {
        because("@SerializedName") // todo move it to report-viewer
    }
}

kotlin {
    explicitApi()
}
