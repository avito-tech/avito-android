plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.libraries")
}

dependencies {
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-extensions"))

    testImplementation(project(":subprojects:gradle:test-project"))
}

gradlePlugin {
    plugins {
        create("robolectricConfig") {
            id = "com.avito.android.robolectric"
            implementationClass = "com.avito.android.plugin.RobolectricPlugin"
            displayName = "Robolectric config"
        }
    }
}
