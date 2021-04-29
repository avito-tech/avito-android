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
        // todo hide parsing
        // todo replace JsonElement with something more generic if possible
        because("module provides TypeAdapterFactory for Entries; JsonElement in the IncidentElement")
    }

    implementation(project(":common:okhttp")) {
        because("Result extension used")
    }
}

kotlin {
    explicitApi()
}
