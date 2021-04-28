plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
    id("convention.libraries")
    id("convention.test-fixtures")
}

dependencies {
    api(libs.okhttp) {
        because("HttpUrl used to type urls more strict")
    }
    api(libs.gson) {
        because("module provides TypeAdapterFactory for Entries")
    }

    implementation(project(":common:okhttp")) {
        because("Result extension used")
    }
}

kotlin {
    explicitApi()
}
